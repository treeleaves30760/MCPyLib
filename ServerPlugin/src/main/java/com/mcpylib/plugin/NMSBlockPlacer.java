package com.mcpylib.plugin;

import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * NMSBlockPlacer — bulk block placement via direct NMS chunk section writes.
 *
 * <p>Bypasses the Bukkit event pipeline, lighting recalculation per-block, and
 * chunk dirty-marking overhead that make {@link Block#setType} slow for large
 * volumes. Blocks are grouped by chunk before any NMS access occurs, so each
 * chunk's section array is touched only once per section per call.
 *
 * <p><b>Version independence</b>: all server internals are reached through
 * reflection / {@link MethodHandle}s that are resolved once, lazily, from the
 * live server objects. The plugin therefore compiles against the public
 * Spigot API only (no BuildTools server jar needed) and does not care whether
 * CraftBukkit's package is relocated. Since Minecraft 26.1 the game ships
 * unobfuscated, so the Mojang names used here are the runtime names on both
 * Paper and Spigot. If any lookup fails (renamed method, non-CraftBukkit
 * server, ...) {@link #isAvailable(World)} returns {@code false} and the caller
 * falls back to the standard Bukkit path.
 *
 * <p><b>Palette contract</b>
 * <ul>
 *   <li>{@code paletteMaterials[i]} — the {@link Material} for palette entry {@code i},
 *       always non-null.</li>
 *   <li>{@code paletteObjects[i]} — the raw {@link JsonObject} for entry {@code i} when it
 *       carries {@code block_state} or {@code nbt} properties; {@code null} for plain
 *       material entries.</li>
 *   <li>{@code paletteIsNull[i]} — {@code true} when the entry represents a skip position
 *       (null in the original block array); such entries are ignored entirely.</li>
 *   <li>{@code flatPaletteIndices} — row-major (x * sizeY * sizeZ + y * sizeZ + z) array
 *       mapping each position in the bounding box to its palette index.</li>
 * </ul>
 *
 * <p><b>Thread safety</b>: must be called on the main server thread; NMS chunk
 * section writes are not thread-safe.
 */
public final class NMSBlockPlacer {

    private static final Logger LOGGER = Logger.getLogger("MCPyLib/NMSBlockPlacer");

    /** Cached result of the first {@link #isAvailable(World)} call. */
    private static volatile Boolean available = null;

    // Resolved handles (all typed as Object-in / Object-out except primitives).
    private static MethodHandle worldGetHandle;      // (CraftWorld) -> ServerLevel
    private static MethodHandle levelGetChunk;       // (ServerLevel, int, int) -> LevelChunk
    private static MethodHandle chunkGetSections;    // (LevelChunk) -> LevelChunkSection[]
    private static MethodHandle sectionSetState;     // (LevelChunkSection, int, int, int, BlockState) -> Object
    private static MethodHandle chunkMarkUnsaved;    // (LevelChunk) -> void   (markUnsaved() or setUnsaved(true))
    private static MethodHandle blockDataGetState;   // (CraftBlockData) -> BlockState

    private NMSBlockPlacer() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when every server-internal method this class needs
     * could be resolved on the running server. Computed once and cached.
     *
     * @param world any loaded world, used to discover the runtime classes
     */
    public static boolean isAvailable(World world) {
        Boolean cached = available;
        if (cached != null) {
            return cached;
        }
        synchronized (NMSBlockPlacer.class) {
            if (available != null) {
                return available;
            }
            try {
                resolve(world);
                available = true;
                LOGGER.info("NMS fast block placement enabled.");
            } catch (Throwable t) {
                LOGGER.warning("NMS fast block placement unavailable, using Bukkit API instead: " + t);
                available = false;
            }
            return available;
        }
    }

    /**
     * Places blocks in bulk using direct NMS chunk-section writes. Entries that
     * carry {@code block_state} or {@code nbt} only get their base type here;
     * CommandHandler applies the state/NBT in a second Bukkit pass.
     *
     * @return total number of blocks placed (NMS + per-chunk Bukkit fallback combined)
     */
    public static int placeBlocks(
            World world,
            int startX, int startY, int startZ,
            int sizeX, int sizeY, int sizeZ,
            Material[] paletteMaterials,
            JsonObject[] paletteObjects,
            boolean[] paletteIsNull,
            int[] flatPaletteIndices) throws Throwable {

        // Phase 1: convert each palette entry to an NMS BlockState once.
        int paletteSize = paletteMaterials.length;
        Object[] nmsStateCache = new Object[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            if (!paletteIsNull[i]) {
                nmsStateCache[i] = toNmsBlockState(paletteMaterials[i]);
            }
        }

        // Phase 2: group positions by chunk.
        // Each entry: { localX, worldY, localZ, paletteIndex, chunkX, chunkZ }
        Map<Long, List<int[]>> chunkMap = new HashMap<>();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    int paletteIdx = flatPaletteIndices[dx * sizeY * sizeZ + dy * sizeZ + dz];
                    if (paletteIsNull[paletteIdx]) {
                        continue;
                    }
                    int worldX = startX + dx;
                    int worldY = startY + dy;
                    int worldZ = startZ + dz;
                    int chunkX = worldX >> 4;
                    int chunkZ = worldZ >> 4;
                    long chunkKey = ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
                    chunkMap
                        .computeIfAbsent(chunkKey, k -> new ArrayList<>())
                        .add(new int[]{worldX & 0xF, worldY, worldZ & 0xF, paletteIdx, chunkX, chunkZ});
                }
            }
        }

        // Phase 3: write blocks per chunk.
        Object serverLevel = worldGetHandle.invoke((Object) world);
        int minSectionIndex = world.getMinHeight() >> 4; // e.g. -4 for minHeight = -64
        Set<Long> affectedChunks = new HashSet<>();
        int count = 0;

        for (Map.Entry<Long, List<int[]>> entry : chunkMap.entrySet()) {
            long chunkKey = entry.getKey();
            List<int[]> positions = entry.getValue();
            int chunkX = (int) (chunkKey & 0xFFFFFFFFL);
            int chunkZ = (int) ((chunkKey >>> 32) & 0xFFFFFFFFL);

            Object nmsChunk;
            Object[] sections;
            try {
                nmsChunk = levelGetChunk.invoke(serverLevel, chunkX, chunkZ);
                sections = (Object[]) chunkGetSections.invoke(nmsChunk);
            } catch (Throwable t) {
                LOGGER.warning("NMSBlockPlacer: could not access chunk (" + chunkX + ", " + chunkZ
                        + "); using Bukkit fallback: " + t);
                for (int[] pos : positions) {
                    Block block = world.getBlockAt(pos[4] * 16 + pos[0], pos[1], pos[5] * 16 + pos[2]);
                    block.setType(paletteMaterials[pos[3]], false);
                    count++;
                }
                continue;
            }

            for (int[] pos : positions) {
                Object nmsState = nmsStateCache[pos[3]];
                if (nmsState == null) {
                    continue;
                }
                int sectionArrayIndex = (pos[1] >> 4) - minSectionIndex;
                if (sectionArrayIndex < 0 || sectionArrayIndex >= sections.length) {
                    continue;
                }
                Object ignored = sectionSetState.invoke(sections[sectionArrayIndex], pos[0], pos[1] & 0xF, pos[2], nmsState);
                count++;
            }

            chunkMarkUnsaved.invoke(nmsChunk);
            affectedChunks.add(chunkKey);
        }

        // Phase 4: resend affected chunks to nearby players.
        for (long chunkKey : affectedChunks) {
            refreshChunk(world, (int) (chunkKey & 0xFFFFFFFFL), (int) ((chunkKey >>> 32) & 0xFFFFFFFFL));
        }

        return count;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    private static void refreshChunk(World world, int chunkX, int chunkZ) {
        // Deprecated in the API but still the only portable way to resend a chunk.
        world.refreshChunk(chunkX, chunkZ);
    }

    private static Object toNmsBlockState(Material material) {
        try {
            return blockDataGetState.invoke((Object) material.createBlockData());
        } catch (Throwable t) {
            LOGGER.warning("NMSBlockPlacer: failed to convert Material." + material.name()
                    + " to NMS BlockState: " + t);
            return null;
        }
    }

    /** Resolves every handle, throwing if anything is missing. */
    private static void resolve(World world) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        // CraftWorld#getHandle() -> ServerLevel
        Method getHandle = findMethod(world.getClass(), "getHandle", 0);
        worldGetHandle = generic(lookup.unreflect(getHandle));
        Object serverLevel = getHandle.invoke(world);

        // Level#getChunk(int, int) -> LevelChunk
        Method getChunk = findMethod(serverLevel.getClass(), "getChunk", int.class, int.class);
        levelGetChunk = generic(lookup.unreflect(getChunk));
        Class<?> chunkClass = getChunk.getReturnType();

        // ChunkAccess#getSections() -> LevelChunkSection[]
        Method getSections = findMethod(chunkClass, "getSections", 0);
        if (!getSections.getReturnType().isArray()) {
            throw new NoSuchMethodException("getSections() does not return an array");
        }
        chunkGetSections = generic(lookup.unreflect(getSections));
        Class<?> sectionClass = getSections.getReturnType().getComponentType();

        // CraftBlockData#getState() -> BlockState
        BlockData sample = Material.STONE.createBlockData();
        Method getState = findMethod(sample.getClass(), "getState", 0);
        blockDataGetState = generic(lookup.unreflect(getState));
        Class<?> blockStateClass = getState.getReturnType();

        // LevelChunkSection#setBlockState(int, int, int, BlockState)
        Method setState = findMethod(sectionClass, "setBlockState", int.class, int.class, int.class, blockStateClass);
        sectionSetState = generic(lookup.unreflect(setState));

        // ChunkAccess#markUnsaved() (1.21.2+) or setUnsaved(boolean) (older)
        MethodHandle mark;
        try {
            mark = lookup.unreflect(findMethod(chunkClass, "markUnsaved", 0));
        } catch (NoSuchMethodException e) {
            MethodHandle setUnsaved = lookup.unreflect(findMethod(chunkClass, "setUnsaved", boolean.class));
            mark = MethodHandles.insertArguments(setUnsaved, 1, true);
        }
        chunkMarkUnsaved = mark.asType(MethodType.methodType(void.class, Object.class));
    }

    /** Adapts a handle so that reference parameters/return are {@code Object}. */
    private static MethodHandle generic(MethodHandle mh) {
        MethodType t = mh.type();
        Class<?>[] params = t.parameterArray();
        for (int i = 0; i < params.length; i++) {
            if (!params[i].isPrimitive()) {
                params[i] = Object.class;
            }
        }
        Class<?> ret = t.returnType();
        if (!ret.isPrimitive()) {
            ret = Object.class;
        }
        return mh.asType(MethodType.methodType(ret, params));
    }

    private static Method findMethod(Class<?> start, String name, int paramCount) throws NoSuchMethodException {
        for (Class<?> c = start; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount
                        && java.lang.reflect.Modifier.isPublic(m.getModifiers())
                        && java.lang.reflect.Modifier.isPublic(m.getDeclaringClass().getModifiers())) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException(start.getName() + "#" + name + " (" + paramCount + " params)");
    }

    private static Method findMethod(Class<?> start, String name, Class<?>... params) throws NoSuchMethodException {
        try {
            Method m = start.getMethod(name, params);
            if (java.lang.reflect.Modifier.isPublic(m.getDeclaringClass().getModifiers())) {
                return m;
            }
        } catch (NoSuchMethodException ignored) {
            // fall through to a hierarchy walk below
        }
        for (Class<?> c = start; c != null; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(name, params);
                if (java.lang.reflect.Modifier.isPublic(m.getModifiers())
                        && java.lang.reflect.Modifier.isPublic(c.getModifiers())) {
                    return m;
                }
            } catch (NoSuchMethodException ignored) {
                // keep walking
            }
        }
        throw new NoSuchMethodException(start.getName() + "#" + name);
    }
}
