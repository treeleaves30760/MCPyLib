package com.mcpylib.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CommandHandler {

    public static CommandResult handleCommand(MCPyLibPlugin plugin, String action, JsonObject params) {
        try {
            switch (action.toLowerCase()) {
                case "setblock":
                    return handleSetBlock(params);
                case "getblock":
                    return handleGetBlock(params);
                case "getblocks":
                    return handleGetBlocks(params);
                case "fill":
                    return handleFill(params);
                case "bulkedit":
                    return handleBulkEdit(params);
                case "getpos":
                    return handleGetPos(params);
                case "teleport":
                    return handleTeleport(params);
                case "gamemode":
                    return handleGamemode(params);
                case "time":
                    return handleTime(params);
                case "weather":
                    return handleWeather(params);
                case "give":
                    return handleGive(params);
                case "summon":
                    return handleSummon(params);
                case "kill":
                    return handleKill(params);
                case "clone":
                    return handleClone(params);
                case "getentitypos":
                    return handleGetEntityPos(params);
                case "getentitystatus":
                    return handleGetEntityStatus(params);
                case "teleportentity":
                    return handleTeleportEntity(params);
                case "setentityvelocity":
                    return handleSetEntityVelocity(params);
                case "setentityrotation":
                    return handleSetEntityRotation(params);
                case "setentityai":
                    return handleSetEntityAI(params);
                case "setentitytarget":
                    return handleSetEntityTarget(params);
                case "removeentity":
                    return handleRemoveEntity(params);
                case "getentityequipment":
                    return handleGetEntityEquipment(params);
                case "setentityequipment":
                    return handleSetEntityEquipment(params);
                case "getvillagerdata":
                    return handleGetVillagerData(params);
                case "setvillagerprofession":
                    return handleSetVillagerProfession(params);
                case "setvillagertrades":
                    return handleSetVillagerTrades(params);
                case "exec":
                    return handleExec(plugin, params);
                case "effect":
                    return handleEffect(params);
                case "cleareffect":
                    return handleClearEffect(params);
                case "clear":
                    return handleClear(params);
                case "experience":
                    return handleExperience(params);
                case "difficulty":
                    return handleDifficulty(params);
                case "gamerule":
                    return handleGamerule(params);
                case "say":
                    return handleSay(params);
                case "tell":
                    return handleTell(params);
                case "tellraw":
                    return handleTellraw(plugin, params);
                case "title":
                    return handleTitle(params);
                case "playsound":
                    return handlePlaysound(params);
                case "stopsound":
                    return handleStopsound(params);
                case "particle":
                    return handleParticle(params);
                case "spawnpoint":
                    return handleSpawnpoint(params);
                case "setworldspawn":
                    return handleSetWorldSpawn(params);
                case "worldborder":
                    return handleWorldBorder(params);
                case "forceload":
                    return handleForceload(params);
                case "damage":
                    return handleDamage(params);
                case "addobjective":
                    return handleAddObjective(params);
                case "removeobjective":
                    return handleRemoveObjective(params);
                case "setscore":
                    return handleSetScore(params);
                case "getscore":
                    return handleGetScore(params);
                case "setdisplayslot":
                    return handleSetDisplaySlot(params);
                case "addtag":
                    return handleAddTag(params);
                case "removetag":
                    return handleRemoveTag(params);
                case "gettags":
                    return handleGetTags(params);
                case "team":
                    return handleTeam(params);
                case "bossbar":
                    return handleBossbar(plugin, params);
                case "attribute":
                    return handleAttribute(params);
                case "enchant":
                    return handleEnchant(params);
                case "getitem":
                    return handleGetItem(params);
                case "setitem":
                    return handleSetItem(params);
                case "locate":
                    return handleLocate(params);
                case "advancement":
                    return handleAdvancement(plugin, params);
                case "loot":
                    return handleLoot(plugin, params);
                case "fillbiome":
                    return handleFillBiome(plugin, params);
                case "placefeature":
                    return handlePlaceFeature(plugin, params);
                case "placestructure":
                    return handlePlaceStructure(plugin, params);
                case "placejigsaw":
                    return handlePlaceJigsaw(plugin, params);
                case "placetemplate":
                    return handlePlaceTemplate(plugin, params);
                case "ride":
                    return handleRide(params);
                case "spreadplayers":
                    return handleSpreadplayers(plugin, params);
                case "defaultgamemode":
                    return handleDefaultGamemode(params);
                case "list":
                    return handleList(params);
                default:
                    return CommandResult.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            return CommandResult.error("Error executing command: " + e.getMessage());
        }
    }

    private static CommandResult handleSetBlock(JsonObject params) {
        // Get parameters
        if (!params.has("x") || !params.has("y") || !params.has("z") || !params.has("block")) {
            return CommandResult.error("Missing parameters: x, y, z, block");
        }

        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();
        String blockName = params.get("block").getAsString();

        // Parse material
        Material material = parseMaterial(blockName);
        if (material == null) {
            return CommandResult.error("Invalid block type: " + blockName);
        }

        // Get world (use overworld by default)
        World world = Bukkit.getWorlds().get(0);

        // Set block
        try {
            Block block = world.getBlockAt(x, y, z);
            block.setType(material);

            // Apply block state if provided
            if (params.has("block_state")) {
                JsonObject blockState = params.getAsJsonObject("block_state");
                BlockData blockData = block.getBlockData();

                // Apply each state property
                for (Map.Entry<String, JsonElement> entry : blockState.entrySet()) {
                    String property = entry.getKey();
                    String value = entry.getValue().getAsString();

                    try {
                        applyBlockState(blockData, property, value);
                    } catch (Exception e) {
                        return CommandResult.error("Failed to set block state '" + property + "': " + e.getMessage());
                    }
                }

                // Update block data
                block.setBlockData(blockData);
            }

            // Apply NBT data if provided
            if (params.has("nbt")) {
                JsonObject nbtData = params.getAsJsonObject("nbt");
                BlockState blockState = block.getState();

                try {
                    applyNBTData(blockState, nbtData);
                    blockState.update(true);
                } catch (Exception e) {
                    return CommandResult.error("Failed to set NBT data: " + e.getMessage());
                }
            }

            return CommandResult.success(1);
        } catch (Exception e) {
            return CommandResult.error("Failed to set block: " + e.getMessage());
        }
    }

    private static void applyBlockState(BlockData blockData, String property, String value) {
        switch (property.toLowerCase()) {
            case "facing":
                if (blockData instanceof Directional) {
                    Directional directional = (Directional) blockData;
                    directional.setFacing(org.bukkit.block.BlockFace.valueOf(value.toUpperCase()));
                }
                break;

            case "rotation":
                if (blockData instanceof Rotatable) {
                    Rotatable rotatable = (Rotatable) blockData;
                    rotatable.setRotation(org.bukkit.block.BlockFace.valueOf(value.toUpperCase()));
                }
                break;

            case "axis":
                if (blockData instanceof Orientable) {
                    Orientable orientable = (Orientable) blockData;
                    orientable.setAxis(org.bukkit.Axis.valueOf(value.toUpperCase()));
                }
                break;

            case "half":
                if (blockData instanceof Stairs) {
                    Stairs stairs = (Stairs) blockData;
                    stairs.setHalf(Stairs.Half.valueOf(value.toUpperCase()));
                } else if (blockData instanceof org.bukkit.block.data.Bisected) {
                    org.bukkit.block.data.Bisected bisected = (org.bukkit.block.data.Bisected) blockData;
                    bisected.setHalf(org.bukkit.block.data.Bisected.Half.valueOf(value.toUpperCase()));
                }
                break;

            case "shape":
                if (blockData instanceof Stairs) {
                    Stairs stairs = (Stairs) blockData;
                    stairs.setShape(Stairs.Shape.valueOf(value.toUpperCase()));
                }
                break;

            case "waterlogged":
                if (blockData instanceof org.bukkit.block.data.Waterlogged) {
                    org.bukkit.block.data.Waterlogged waterlogged = (org.bukkit.block.data.Waterlogged) blockData;
                    waterlogged.setWaterlogged(Boolean.parseBoolean(value));
                }
                break;

            case "open":
                if (blockData instanceof org.bukkit.block.data.Openable) {
                    org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) blockData;
                    openable.setOpen(Boolean.parseBoolean(value));
                }
                break;

            case "powered":
                if (blockData instanceof org.bukkit.block.data.Powerable) {
                    org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) blockData;
                    powerable.setPowered(Boolean.parseBoolean(value));
                }
                break;

            default:
                // Try to use the generic BlockData merge method
                String stateString = property + "=" + value;
                try {
                    BlockData newData = Bukkit.createBlockData(blockData.getMaterial(), stateString);
                    blockData.merge(newData);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unsupported property: " + property);
                }
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private static void applyNBTData(BlockState blockState, JsonObject nbtData) {
        // Note: Bukkit doesn't provide direct NBT access in the same way as vanilla Minecraft
        // This is a simplified implementation that handles common cases using legacy Bukkit API

        // Handle CustomName for containers and other block entities
        if (nbtData.has("CustomName")) {
            String customName = nbtData.get("CustomName").getAsString();

            // Use legacy API for compatibility
            if (blockState instanceof org.bukkit.block.Container) {
                try {
                    ((org.bukkit.block.Container) blockState).setCustomName(customName);
                } catch (Exception e) {
                    // Fallback: CustomName might not be supported in all versions
                }
            }
        }

        // Handle specific block entity types
        if (blockState instanceof org.bukkit.block.Sign) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;

            // Use legacy setLine methods for compatibility
            if (nbtData.has("Text1")) {
                sign.setLine(0, nbtData.get("Text1").getAsString());
            }
            if (nbtData.has("Text2")) {
                sign.setLine(1, nbtData.get("Text2").getAsString());
            }
            if (nbtData.has("Text3")) {
                sign.setLine(2, nbtData.get("Text3").getAsString());
            }
            if (nbtData.has("Text4")) {
                sign.setLine(3, nbtData.get("Text4").getAsString());
            }
        }

        // Handle chest/container contents (simplified)
        if (blockState instanceof org.bukkit.block.Chest && nbtData.has("Items")) {
            // This would require more complex NBT parsing
            // Left as placeholder for future implementation
        }

        // For more complex NBT data (detailed items, complex structures), you would need to use
        // NMS (net.minecraft.server) or additional libraries like NBT-API
        // This is left as a basic implementation that can be extended based on specific needs
    }

    private static CommandResult handleGetBlock(JsonObject params) {
        // Get parameters
        if (!params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: x, y, z");
        }

        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();

        // Get world
        World world = Bukkit.getWorlds().get(0);

        // Get block
        try {
            Block block = world.getBlockAt(x, y, z);
            String blockType = "minecraft:" + block.getType().name().toLowerCase();
            return CommandResult.success(blockType);
        } catch (Exception e) {
            return CommandResult.error("Failed to get block: " + e.getMessage());
        }
    }

    private static CommandResult handleGetBlocks(JsonObject params) {
        // Get parameters
        if (!params.has("x1") || !params.has("y1") || !params.has("z1") ||
            !params.has("x2") || !params.has("y2") || !params.has("z2")) {
            return CommandResult.error("Missing parameters: x1, y1, z1, x2, y2, z2");
        }

        int x1 = params.get("x1").getAsInt();
        int y1 = params.get("y1").getAsInt();
        int z1 = params.get("z1").getAsInt();
        int x2 = params.get("x2").getAsInt();
        int y2 = params.get("y2").getAsInt();
        int z2 = params.get("z2").getAsInt();

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        // Get world
        World world = Bukkit.getWorlds().get(0);

        // Build 3D array [x][y][z]
        try {
            java.util.List<java.util.List<java.util.List<String>>> result = new java.util.ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                java.util.List<java.util.List<String>> xLayer = new java.util.ArrayList<>();
                for (int y = minY; y <= maxY; y++) {
                    java.util.List<String> yLayer = new java.util.ArrayList<>();
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        String blockType = "minecraft:" + block.getType().name().toLowerCase();
                        yLayer.add(blockType);
                    }
                    xLayer.add(yLayer);
                }
                result.add(xLayer);
            }
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to get blocks: " + e.getMessage());
        }
    }

    private static CommandResult handleFill(JsonObject params) {
        // Get parameters
        if (!params.has("x1") || !params.has("y1") || !params.has("z1") ||
            !params.has("x2") || !params.has("y2") || !params.has("z2") ||
            !params.has("block")) {
            return CommandResult.error("Missing parameters: x1, y1, z1, x2, y2, z2, block");
        }

        int x1 = params.get("x1").getAsInt();
        int y1 = params.get("y1").getAsInt();
        int z1 = params.get("z1").getAsInt();
        int x2 = params.get("x2").getAsInt();
        int y2 = params.get("y2").getAsInt();
        int z2 = params.get("z2").getAsInt();
        String blockName = params.get("block").getAsString();

        // Parse material
        Material material = parseMaterial(blockName);
        if (material == null) {
            return CommandResult.error("Invalid block type: " + blockName);
        }

        // Get world
        World world = Bukkit.getWorlds().get(0);

        // Fill region
        try {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            int count = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(material, false);
                        count++;
                    }
                }
            }

            return CommandResult.success(count);
        } catch (Exception e) {
            return CommandResult.error("Failed to fill region: " + e.getMessage());
        }
    }

    private static CommandResult handleBulkEdit(JsonObject params) {
        // Get parameters
        if (!params.has("x") || !params.has("y") || !params.has("z") || !params.has("blocks")) {
            return CommandResult.error("Missing parameters: x, y, z, blocks");
        }

        int startX = params.get("x").getAsInt();
        int startY = params.get("y").getAsInt();
        int startZ = params.get("z").getAsInt();
        JsonArray blocks = params.getAsJsonArray("blocks");

        // Get world
        World world = Bukkit.getWorlds().get(0);

        try {
            int count = 0;
            int sizeX = blocks.size();

            // Iterate through 3D array: blocks[x][y][z]
            for (int dx = 0; dx < sizeX; dx++) {
                JsonElement xElement = blocks.get(dx);
                if (!xElement.isJsonArray()) {
                    return CommandResult.error("Invalid blocks format: expected 3D array at index " + dx);
                }
                JsonArray yArray = xElement.getAsJsonArray();
                int sizeY = yArray.size();

                for (int dy = 0; dy < sizeY; dy++) {
                    JsonElement yElement = yArray.get(dy);
                    if (!yElement.isJsonArray()) {
                        return CommandResult.error("Invalid blocks format: expected 3D array at [" + dx + "][" + dy + "]");
                    }
                    JsonArray zArray = yElement.getAsJsonArray();
                    int sizeZ = zArray.size();

                    for (int dz = 0; dz < sizeZ; dz++) {
                        JsonElement blockElement = zArray.get(dz);

                        // Skip null elements (air or skip position)
                        if (blockElement.isJsonNull()) {
                            continue;
                        }

                        // Calculate world coordinates
                        int worldX = startX + dx;
                        int worldY = startY + dy;
                        int worldZ = startZ + dz;

                        // Get block at position
                        Block block = world.getBlockAt(worldX, worldY, worldZ);

                        // Handle different element types (mixed mode)
                        if (blockElement.isJsonPrimitive() && blockElement.getAsJsonPrimitive().isString()) {
                            // Simple string: just block name
                            String blockName = blockElement.getAsString();
                            Material material = parseMaterial(blockName);
                            if (material == null) {
                                return CommandResult.error("Invalid block type at [" + dx + "][" + dy + "][" + dz + "]: " + blockName);
                            }
                            block.setType(material, false);
                            count++;
                        } else if (blockElement.isJsonObject()) {
                            // Complex object: block, block_state, nbt
                            JsonObject blockData = blockElement.getAsJsonObject();

                            if (!blockData.has("block")) {
                                return CommandResult.error("Missing 'block' field at [" + dx + "][" + dy + "][" + dz + "]");
                            }

                            String blockName = blockData.get("block").getAsString();
                            Material material = parseMaterial(blockName);
                            if (material == null) {
                                return CommandResult.error("Invalid block type at [" + dx + "][" + dy + "][" + dz + "]: " + blockName);
                            }

                            // Set block type
                            block.setType(material, false);

                            // Apply block state if provided
                            if (blockData.has("block_state")) {
                                JsonObject blockState = blockData.getAsJsonObject("block_state");
                                BlockData bukkitBlockData = block.getBlockData();

                                for (Map.Entry<String, JsonElement> entry : blockState.entrySet()) {
                                    String property = entry.getKey();
                                    String value = entry.getValue().getAsString();

                                    try {
                                        applyBlockState(bukkitBlockData, property, value);
                                    } catch (Exception e) {
                                        return CommandResult.error("Failed to set block state at [" + dx + "][" + dy + "][" + dz + "]: " + e.getMessage());
                                    }
                                }

                                block.setBlockData(bukkitBlockData);
                            }

                            // Apply NBT data if provided
                            if (blockData.has("nbt")) {
                                JsonObject nbtData = blockData.getAsJsonObject("nbt");
                                BlockState blockState = block.getState();

                                try {
                                    applyNBTData(blockState, nbtData);
                                    blockState.update(true);
                                } catch (Exception e) {
                                    return CommandResult.error("Failed to set NBT data at [" + dx + "][" + dy + "][" + dz + "]: " + e.getMessage());
                                }
                            }

                            count++;
                        } else {
                            return CommandResult.error("Invalid block element at [" + dx + "][" + dy + "][" + dz + "]: must be string or object");
                        }
                    }
                }
            }

            return CommandResult.success(count);
        } catch (Exception e) {
            return CommandResult.error("Failed to bulk edit: " + e.getMessage());
        }
    }

    private static CommandResult handleGetPos(JsonObject params) {
        // Get parameters
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();

        // Find player
        Player player = Bukkit.getPlayerExact(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        // Get position
        Location loc = player.getLocation();
        int[] position = new int[] {
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        };

        return CommandResult.success(position);
    }

    private static CommandResult handleTeleport(JsonObject params) {
        // Validate parameters
        if (!params.has("username") || !params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: username, x, y, z");
        }

        String username = params.get("username").getAsString();
        double x = params.get("x").getAsDouble();
        double y = params.get("y").getAsDouble();
        double z = params.get("z").getAsDouble();

        // Find player
        Player player = Bukkit.getPlayerExact(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        // Execute Bukkit API call
        World world = player.getWorld();
        Location location = new Location(world, x, y, z);

        // Handle optional parameters (yaw/pitch)
        if (params.has("yaw") && params.has("pitch")) {
            float yaw = params.get("yaw").getAsFloat();
            float pitch = params.get("pitch").getAsFloat();
            location.setYaw(yaw);
            location.setPitch(pitch);
        }

        try {
            boolean success = player.teleport(location);
            if (success) {
                return CommandResult.success(true);
            } else {
                return CommandResult.error("Teleport failed");
            }
        } catch (Exception e) {
            return CommandResult.error("Failed to teleport: " + e.getMessage());
        }
    }

    private static CommandResult handleGamemode(JsonObject params) {
        // Validate parameters
        if (!params.has("username") || !params.has("mode")) {
            return CommandResult.error("Missing parameters: username, mode");
        }

        String username = params.get("username").getAsString();
        String modeStr = params.get("mode").getAsString();

        // Find player
        Player player = Bukkit.getPlayerExact(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        // Parse gamemode
        GameMode mode;
        try {
            mode = GameMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid gamemode: " + modeStr + " (valid: survival, creative, adventure, spectator)");
        }

        // Set gamemode
        try {
            player.setGameMode(mode);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set gamemode: " + e.getMessage());
        }
    }

    private static CommandResult handleTime(JsonObject params) {
        // Validate parameters
        if (!params.has("action")) {
            return CommandResult.error("Missing parameter: action");
        }

        String action = params.get("action").getAsString().toLowerCase();
        World world = Bukkit.getWorlds().get(0);

        try {
            switch (action) {
                case "set":
                    if (!params.has("value")) {
                        return CommandResult.error("Missing parameter: value for action 'set'");
                    }
                    long setValue = params.get("value").getAsLong();
                    if (setValue < 0 || setValue > 24000) {
                        return CommandResult.error("Value out of range: " + setValue + " (valid: 0-24000)");
                    }
                    world.setTime(setValue);
                    return CommandResult.success(world.getTime());

                case "add":
                    if (!params.has("value")) {
                        return CommandResult.error("Missing parameter: value for action 'add'");
                    }
                    long addValue = params.get("value").getAsLong();
                    world.setTime(world.getTime() + addValue);
                    return CommandResult.success(world.getTime());

                case "query":
                    return CommandResult.success(world.getTime());

                default:
                    return CommandResult.error("Invalid action: " + action + " (valid: set, add, query)");
            }
        } catch (Exception e) {
            return CommandResult.error("Failed to modify time: " + e.getMessage());
        }
    }

    private static CommandResult handleWeather(JsonObject params) {
        // Validate parameters
        if (!params.has("condition")) {
            return CommandResult.error("Missing parameter: condition");
        }

        String condition = params.get("condition").getAsString().toLowerCase();
        World world = Bukkit.getWorlds().get(0);

        try {
            switch (condition) {
                case "clear":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;

                case "rain":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;

                case "thunder":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;

                default:
                    return CommandResult.error("Invalid condition: " + condition + " (valid: clear, rain, thunder)");
            }

            // Set duration if provided (convert seconds to ticks)
            if (params.has("duration")) {
                int durationSeconds = params.get("duration").getAsInt();
                int durationTicks = durationSeconds * 20;
                world.setWeatherDuration(durationTicks);
            }

            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set weather: " + e.getMessage());
        }
    }

    private static CommandResult handleGive(JsonObject params) {
        // Validate parameters
        if (!params.has("username") || !params.has("item")) {
            return CommandResult.error("Missing parameters: username, item");
        }

        String username = params.get("username").getAsString();
        String itemName = params.get("item").getAsString();
        int amount = params.has("amount") ? params.get("amount").getAsInt() : 1;

        // Validate amount
        if (amount < 1 || amount > 64) {
            return CommandResult.error("Amount must be between 1 and 64");
        }

        // Find player
        Player player = Bukkit.getPlayerExact(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        // Parse material
        Material material = parseMaterial(itemName);
        if (material == null) {
            return CommandResult.error("Invalid item type: " + itemName);
        }

        // Check if material is an item (not air)
        if (material == Material.AIR) {
            return CommandResult.error("Cannot give air");
        }

        // Give item to player
        try {
            ItemStack itemStack = new ItemStack(material, amount);
            player.getInventory().addItem(itemStack);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to give item: " + e.getMessage());
        }
    }

    private static CommandResult handleSummon(JsonObject params) {
        // Validate parameters
        if (!params.has("entity_type") || !params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: entity_type, x, y, z");
        }

        String entityTypeName = params.get("entity_type").getAsString();
        double x = params.get("x").getAsDouble();
        double y = params.get("y").getAsDouble();
        double z = params.get("z").getAsDouble();

        // Remove "minecraft:" prefix if present
        if (entityTypeName.startsWith("minecraft:")) {
            entityTypeName = entityTypeName.substring("minecraft:".length());
        }

        // Parse entity type
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid entity type: " + entityTypeName);
        }

        // Check if entity type is spawnable
        if (entityType == EntityType.PLAYER || !entityType.isSpawnable()) {
            return CommandResult.error("Cannot summon entity type: " + entityTypeName);
        }

        // Get world and location
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, x, y, z);

        // Summon entity
        try {
            Entity entity = world.spawnEntity(location, entityType);
            return CommandResult.success(entity.getUniqueId().toString());
        } catch (Exception e) {
            return CommandResult.error("Failed to summon entity: " + e.getMessage());
        }
    }

    private static CommandResult handleKill(JsonObject params) {
        // Validate parameters
        if (!params.has("selector")) {
            return CommandResult.error("Missing parameter: selector");
        }

        String selector = params.get("selector").getAsString();
        World world = Bukkit.getWorlds().get(0);
        int count = 0;
        int limit = 1000; // Max entities to kill

        try {
            if (selector.equalsIgnoreCase("all")) {
                // Kill all entities except players
                for (Entity entity : world.getEntities()) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                        count++;
                        if (count >= limit) break;
                    }
                }
            } else if (selector.startsWith("player:")) {
                // Kill specific player
                String username = selector.substring("player:".length());
                Player player = Bukkit.getPlayerExact(username);
                if (player == null) {
                    return CommandResult.error("Player not found: " + username);
                }
                player.setHealth(0.0);
                count = 1;
            } else {
                // Kill entities of specific type
                String entityTypeName = selector;
                if (entityTypeName.startsWith("minecraft:")) {
                    entityTypeName = entityTypeName.substring("minecraft:".length());
                }

                EntityType targetType;
                try {
                    targetType = EntityType.valueOf(entityTypeName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return CommandResult.error("Invalid selector: " + selector + " (use 'all', 'player:username', or entity type)");
                }

                // Remove entities of this type
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() == targetType) {
                        entity.remove();
                        count++;
                        if (count >= limit) break;
                    }
                }
            }

            return CommandResult.success(count);
        } catch (Exception e) {
            return CommandResult.error("Failed to kill entities: " + e.getMessage());
        }
    }

    private static CommandResult handleClone(JsonObject params) {
        // Validate parameters
        if (!params.has("x1") || !params.has("y1") || !params.has("z1") ||
            !params.has("x2") || !params.has("y2") || !params.has("z2") ||
            !params.has("dest_x") || !params.has("dest_y") || !params.has("dest_z")) {
            return CommandResult.error("Missing parameters: x1, y1, z1, x2, y2, z2, dest_x, dest_y, dest_z");
        }

        int x1 = params.get("x1").getAsInt();
        int y1 = params.get("y1").getAsInt();
        int z1 = params.get("z1").getAsInt();
        int x2 = params.get("x2").getAsInt();
        int y2 = params.get("y2").getAsInt();
        int z2 = params.get("z2").getAsInt();
        int destX = params.get("dest_x").getAsInt();
        int destY = params.get("dest_y").getAsInt();
        int destZ = params.get("dest_z").getAsInt();

        // Calculate bounds
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        // Check region size (limit to 32768 blocks)
        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > 32768) {
            return CommandResult.error("Region too large (max 32768 blocks): " + volume);
        }

        World world = Bukkit.getWorlds().get(0);
        int count = 0;

        try {
            // Clone blocks from source to destination
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // Calculate offset from source corner
                        int offsetX = x - minX;
                        int offsetY = y - minY;
                        int offsetZ = z - minZ;

                        // Calculate destination coordinates
                        int newX = destX + offsetX;
                        int newY = destY + offsetY;
                        int newZ = destZ + offsetZ;

                        // Get source block
                        Block sourceBlock = world.getBlockAt(x, y, z);
                        BlockData sourceData = sourceBlock.getBlockData();

                        // Set destination block
                        Block destBlock = world.getBlockAt(newX, newY, newZ);
                        destBlock.setBlockData(sourceData);

                        count++;
                    }
                }
            }

            return CommandResult.success(count);
        } catch (Exception e) {
            return CommandResult.error("Failed to clone region: " + e.getMessage());
        }
    }

    private static Material parseMaterial(String blockName) {
        // Remove "minecraft:" prefix if present
        if (blockName.startsWith("minecraft:")) {
            blockName = blockName.substring("minecraft:".length());
        }

        // Convert to uppercase for Material enum
        String materialName = blockName.toUpperCase();

        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Entity findEntityByUUID(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private static CommandResult handleGetEntityPos(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        Location loc = entity.getLocation();
        Map<String, Double> position = new HashMap<>();
        position.put("x", loc.getX());
        position.put("y", loc.getY());
        position.put("z", loc.getZ());
        position.put("yaw", (double) loc.getYaw());
        position.put("pitch", (double) loc.getPitch());

        return CommandResult.success(position);
    }

    private static CommandResult handleGetEntityStatus(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        Map<String, Object> status = new HashMap<>();
        status.put("uuid", entity.getUniqueId().toString());
        status.put("type", "minecraft:" + entity.getType().name().toLowerCase());
        status.put("custom_name", entity.getCustomName());
        status.put("is_valid", entity.isValid());
        status.put("is_dead", entity.isDead());
        status.put("world", entity.getWorld().getName());

        // Position
        Location loc = entity.getLocation();
        Map<String, Double> position = new HashMap<>();
        position.put("x", loc.getX());
        position.put("y", loc.getY());
        position.put("z", loc.getZ());
        status.put("position", position);

        // Velocity
        Vector vel = entity.getVelocity();
        Map<String, Double> velocity = new HashMap<>();
        velocity.put("x", vel.getX());
        velocity.put("y", vel.getY());
        velocity.put("z", vel.getZ());
        status.put("velocity", velocity);

        // LivingEntity specific properties
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            status.put("health", living.getHealth());
            try {
                status.put("max_health", living.getAttribute(Attribute.MAX_HEALTH).getValue());
            } catch (Exception e) {
                status.put("max_health", null);
            }
            status.put("has_ai", living.hasAI());
        } else {
            status.put("health", null);
            status.put("max_health", null);
            status.put("has_ai", null);
        }

        return CommandResult.success(status);
    }

    private static CommandResult handleTeleportEntity(JsonObject params) {
        if (!params.has("uuid") || !params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: uuid, x, y, z");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        double x = params.get("x").getAsDouble();
        double y = params.get("y").getAsDouble();
        double z = params.get("z").getAsDouble();

        Location location = new Location(entity.getWorld(), x, y, z);

        // Handle optional yaw/pitch
        if (params.has("yaw") && params.has("pitch")) {
            float yaw = params.get("yaw").getAsFloat();
            float pitch = params.get("pitch").getAsFloat();
            location.setYaw(yaw);
            location.setPitch(pitch);
        } else {
            // Preserve current rotation
            Location currentLoc = entity.getLocation();
            location.setYaw(currentLoc.getYaw());
            location.setPitch(currentLoc.getPitch());
        }

        try {
            boolean success = entity.teleport(location);
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to teleport entity: " + e.getMessage());
        }
    }

    private static CommandResult handleSetEntityVelocity(JsonObject params) {
        if (!params.has("uuid") || !params.has("vx") || !params.has("vy") || !params.has("vz")) {
            return CommandResult.error("Missing parameters: uuid, vx, vy, vz");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        double vx = params.get("vx").getAsDouble();
        double vy = params.get("vy").getAsDouble();
        double vz = params.get("vz").getAsDouble();

        // Limit velocity to prevent server crashes (max ±10 blocks/tick)
        double maxVelocity = 10.0;
        vx = Math.max(-maxVelocity, Math.min(maxVelocity, vx));
        vy = Math.max(-maxVelocity, Math.min(maxVelocity, vy));
        vz = Math.max(-maxVelocity, Math.min(maxVelocity, vz));

        try {
            entity.setVelocity(new Vector(vx, vy, vz));
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set entity velocity: " + e.getMessage());
        }
    }

    private static CommandResult handleSetEntityRotation(JsonObject params) {
        if (!params.has("uuid") || !params.has("yaw") || !params.has("pitch")) {
            return CommandResult.error("Missing parameters: uuid, yaw, pitch");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        float yaw = params.get("yaw").getAsFloat();
        float pitch = params.get("pitch").getAsFloat();

        try {
            Location loc = entity.getLocation();
            loc.setYaw(yaw);
            loc.setPitch(pitch);
            entity.teleport(loc);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set entity rotation: " + e.getMessage());
        }
    }

    private static CommandResult handleSetEntityAI(JsonObject params) {
        if (!params.has("uuid") || !params.has("enabled")) {
            return CommandResult.error("Missing parameters: uuid, enabled");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof LivingEntity)) {
            return CommandResult.error("Entity is not a LivingEntity: " + uuidStr);
        }

        boolean enabled = params.get("enabled").getAsBoolean();

        try {
            ((LivingEntity) entity).setAI(enabled);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set entity AI: " + e.getMessage());
        }
    }

    private static CommandResult handleSetEntityTarget(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof Mob)) {
            return CommandResult.error("Entity is not a Mob (cannot have a target): " + uuidStr);
        }

        Mob mob = (Mob) entity;

        // If target_uuid is provided, set the target; otherwise clear it
        if (params.has("target_uuid") && !params.get("target_uuid").isJsonNull()) {
            String targetUuidStr = params.get("target_uuid").getAsString();
            UUID targetUuid;
            try {
                targetUuid = UUID.fromString(targetUuidStr);
            } catch (IllegalArgumentException e) {
                return CommandResult.error("Invalid target UUID format: " + targetUuidStr);
            }

            Entity targetEntity = findEntityByUUID(targetUuid);
            if (targetEntity == null) {
                return CommandResult.error("Target entity not found: " + targetUuidStr);
            }

            if (!(targetEntity instanceof LivingEntity)) {
                return CommandResult.error("Target entity is not a LivingEntity: " + targetUuidStr);
            }

            try {
                mob.setTarget((LivingEntity) targetEntity);
                return CommandResult.success(true);
            } catch (Exception e) {
                return CommandResult.error("Failed to set entity target: " + e.getMessage());
            }
        } else {
            // Clear target
            try {
                mob.setTarget(null);
                return CommandResult.success(true);
            } catch (Exception e) {
                return CommandResult.error("Failed to clear entity target: " + e.getMessage());
            }
        }
    }

    private static CommandResult handleRemoveEntity(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        // Prevent removing players
        if (entity instanceof Player) {
            return CommandResult.error("Cannot remove players using removeEntity");
        }

        try {
            entity.remove();
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to remove entity: " + e.getMessage());
        }
    }

    private static CommandResult handleGetEntityEquipment(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof LivingEntity)) {
            return CommandResult.error("Entity does not support equipment: " + uuidStr);
        }

        EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
        if (equipment == null) {
            return CommandResult.error("Entity equipment is null");
        }

        Map<String, String> result = new HashMap<>();
        result.put("helmet", itemStackToString(equipment.getHelmet()));
        result.put("chestplate", itemStackToString(equipment.getChestplate()));
        result.put("leggings", itemStackToString(equipment.getLeggings()));
        result.put("boots", itemStackToString(equipment.getBoots()));
        result.put("main_hand", itemStackToString(equipment.getItemInMainHand()));
        result.put("off_hand", itemStackToString(equipment.getItemInOffHand()));

        return CommandResult.success(result);
    }

    private static String itemStackToString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        return "minecraft:" + item.getType().name().toLowerCase();
    }

    private static Map<String, Object> itemStackToMap(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("item", "minecraft:" + item.getType().name().toLowerCase());
        map.put("amount", item.getAmount());
        return map;
    }

    private static ItemStack createItemStack(JsonObject itemData) {
        String itemName = itemData.get("item").getAsString();
        int amount = itemData.has("amount") ? itemData.get("amount").getAsInt() : 1;

        Material material = parseMaterial(itemName);
        if (material == null) {
            throw new IllegalArgumentException("Invalid item type: " + itemName);
        }

        return new ItemStack(material, amount);
    }

    private static CommandResult handleSetEntityEquipment(JsonObject params) {
        if (!params.has("uuid") || !params.has("equipment")) {
            return CommandResult.error("Missing parameters: uuid, equipment");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof LivingEntity)) {
            return CommandResult.error("Entity does not support equipment: " + uuidStr);
        }

        EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
        if (equipment == null) {
            return CommandResult.error("Entity equipment is null");
        }

        JsonObject equipmentData = params.getAsJsonObject("equipment");

        try {
            // Set each equipment slot if specified
            if (equipmentData.has("helmet") && !equipmentData.get("helmet").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("helmet").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid helmet material");
                }
                equipment.setHelmet(new ItemStack(mat));
            }

            if (equipmentData.has("chestplate") && !equipmentData.get("chestplate").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("chestplate").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid chestplate material");
                }
                equipment.setChestplate(new ItemStack(mat));
            }

            if (equipmentData.has("leggings") && !equipmentData.get("leggings").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("leggings").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid leggings material");
                }
                equipment.setLeggings(new ItemStack(mat));
            }

            if (equipmentData.has("boots") && !equipmentData.get("boots").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("boots").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid boots material");
                }
                equipment.setBoots(new ItemStack(mat));
            }

            if (equipmentData.has("main_hand") && !equipmentData.get("main_hand").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("main_hand").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid main_hand material");
                }
                equipment.setItemInMainHand(new ItemStack(mat));
            }

            if (equipmentData.has("off_hand") && !equipmentData.get("off_hand").isJsonNull()) {
                Material mat = parseMaterial(equipmentData.get("off_hand").getAsString());
                if (mat == null) {
                    return CommandResult.error("Invalid off_hand material");
                }
                equipment.setItemInOffHand(new ItemStack(mat));
            }

            // Set drop chances if provided
            if (params.has("drop_chances")) {
                JsonObject dropChances = params.getAsJsonObject("drop_chances");

                if (dropChances.has("helmet")) {
                    equipment.setHelmetDropChance(dropChances.get("helmet").getAsFloat());
                }
                if (dropChances.has("chestplate")) {
                    equipment.setChestplateDropChance(dropChances.get("chestplate").getAsFloat());
                }
                if (dropChances.has("leggings")) {
                    equipment.setLeggingsDropChance(dropChances.get("leggings").getAsFloat());
                }
                if (dropChances.has("boots")) {
                    equipment.setBootsDropChance(dropChances.get("boots").getAsFloat());
                }
                if (dropChances.has("main_hand")) {
                    equipment.setItemInMainHandDropChance(dropChances.get("main_hand").getAsFloat());
                }
                if (dropChances.has("off_hand")) {
                    equipment.setItemInOffHandDropChance(dropChances.get("off_hand").getAsFloat());
                }
            }

            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set entity equipment: " + e.getMessage());
        }
    }

    private static CommandResult handleGetVillagerData(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof Villager)) {
            return CommandResult.error("Entity is not a Villager: " + uuidStr);
        }

        Villager villager = (Villager) entity;
        Map<String, Object> result = new HashMap<>();

        result.put("profession", villager.getProfession().name());
        result.put("level", villager.getVillagerLevel());

        // Get trade list
        List<Map<String, Object>> trades = new ArrayList<>();
        for (MerchantRecipe recipe : villager.getRecipes()) {
            Map<String, Object> trade = new HashMap<>();

            List<ItemStack> ingredients = recipe.getIngredients();
            if (ingredients.size() > 0) {
                trade.put("buy1", itemStackToMap(ingredients.get(0)));
            }
            if (ingredients.size() > 1) {
                trade.put("buy2", itemStackToMap(ingredients.get(1)));
            } else {
                trade.put("buy2", null);
            }
            trade.put("sell", itemStackToMap(recipe.getResult()));
            trade.put("uses", recipe.getUses());
            trade.put("max_uses", recipe.getMaxUses());

            trades.add(trade);
        }
        result.put("trades", trades);

        return CommandResult.success(result);
    }

    private static CommandResult handleSetVillagerProfession(JsonObject params) {
        if (!params.has("uuid") || !params.has("profession")) {
            return CommandResult.error("Missing parameters: uuid, profession");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof Villager)) {
            return CommandResult.error("Entity is not a Villager: " + uuidStr);
        }

        String professionStr = params.get("profession").getAsString();
        Villager.Profession profession;
        try {
            profession = Villager.Profession.valueOf(professionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid profession: " + professionStr +
                " (valid: ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN, FLETCHER, " +
                "LEATHERWORKER, LIBRARIAN, MASON, NITWIT, NONE, SHEPHERD, TOOLSMITH, WEAPONSMITH)");
        }

        try {
            ((Villager) entity).setProfession(profession);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set villager profession: " + e.getMessage());
        }
    }

    private static CommandResult handleSetVillagerTrades(JsonObject params) {
        if (!params.has("uuid") || !params.has("trades")) {
            return CommandResult.error("Missing parameters: uuid, trades");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof Villager)) {
            return CommandResult.error("Entity is not a Villager: " + uuidStr);
        }

        Villager villager = (Villager) entity;
        JsonArray tradesArray = params.getAsJsonArray("trades");

        try {
            List<MerchantRecipe> recipes = new ArrayList<>();

            for (JsonElement tradeElement : tradesArray) {
                JsonObject tradeData = tradeElement.getAsJsonObject();

                // Parse sell item (result)
                if (!tradeData.has("sell")) {
                    return CommandResult.error("Trade missing 'sell' field");
                }
                JsonObject sellData = tradeData.getAsJsonObject("sell");
                ItemStack result = createItemStack(sellData);

                // Create MerchantRecipe
                int maxUses = tradeData.has("max_uses") ?
                    tradeData.get("max_uses").getAsInt() : 10;
                boolean experienceReward = !tradeData.has("experience_reward") ||
                    tradeData.get("experience_reward").getAsBoolean();

                MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
                recipe.setExperienceReward(experienceReward);

                // Add first ingredient (buy1)
                if (!tradeData.has("buy1")) {
                    return CommandResult.error("Trade missing 'buy1' field");
                }
                JsonObject buy1Data = tradeData.getAsJsonObject("buy1");
                recipe.addIngredient(createItemStack(buy1Data));

                // Add second ingredient if provided (buy2)
                if (tradeData.has("buy2") && !tradeData.get("buy2").isJsonNull()) {
                    JsonObject buy2Data = tradeData.getAsJsonObject("buy2");
                    recipe.addIngredient(createItemStack(buy2Data));
                }

                recipes.add(recipe);
            }

            villager.setRecipes(recipes);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set villager trades: " + e.getMessage());
        }
    }

    // ===== Phase 0: exec (generic command executor) =====
    private static CommandResult handleExec(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("command")) {
            return CommandResult.error("Missing parameter: command");
        }
        String command = params.get("command").getAsString();
        // Remove leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        try {
            final String cmd = command;
            // Must run on main thread
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to execute command: " + e.getMessage());
        }
    }

    // ===== Phase 1: Player effects & status =====
    private static CommandResult handleEffect(JsonObject params) {
        if (!params.has("username") || !params.has("effect")) {
            return CommandResult.error("Missing parameters: username, effect");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String effectName = params.get("effect").getAsString();
        if (effectName.startsWith("minecraft:")) {
            effectName = effectName.substring(10);
        }

        org.bukkit.potion.PotionEffectType effectType = org.bukkit.potion.PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) {
            return CommandResult.error("Invalid effect type: " + effectName);
        }

        int duration = params.has("duration") ? params.get("duration").getAsInt() * 20 : 600; // default 30 sec, convert to ticks
        int amplifier = params.has("amplifier") ? params.get("amplifier").getAsInt() : 0;
        boolean hideParticles = params.has("hide_particles") && params.get("hide_particles").getAsBoolean();

        try {
            org.bukkit.potion.PotionEffect potionEffect = new org.bukkit.potion.PotionEffect(
                effectType, duration, amplifier, false, !hideParticles
            );
            boolean result = player.addPotionEffect(potionEffect);
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to apply effect: " + e.getMessage());
        }
    }

    private static CommandResult handleClearEffect(JsonObject params) {
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        if (params.has("effect") && !params.get("effect").isJsonNull()) {
            String effectName = params.get("effect").getAsString();
            if (effectName.startsWith("minecraft:")) {
                effectName = effectName.substring(10);
            }
            org.bukkit.potion.PotionEffectType effectType = org.bukkit.potion.PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType == null) {
                return CommandResult.error("Invalid effect type: " + effectName);
            }
            player.removePotionEffect(effectType);
        } else {
            // Clear all effects
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }

        return CommandResult.success(true);
    }

    private static CommandResult handleClear(JsonObject params) {
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String itemFilter = params.has("item") && !params.get("item").isJsonNull() ?
            params.get("item").getAsString() : null;
        int maxCount = params.has("max_count") ? params.get("max_count").getAsInt() : -1;

        Material filterMaterial = null;
        if (itemFilter != null) {
            filterMaterial = parseMaterial(itemFilter);
            if (filterMaterial == null) {
                return CommandResult.error("Invalid item type: " + itemFilter);
            }
        }

        int removed = 0;
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            if (maxCount >= 0 && removed >= maxCount) break;

            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType() == Material.AIR) continue;

            if (filterMaterial != null && stack.getType() != filterMaterial) continue;

            if (maxCount < 0) {
                removed += stack.getAmount();
                inv.setItem(i, null);
            } else {
                int canRemove = maxCount - removed;
                if (stack.getAmount() <= canRemove) {
                    removed += stack.getAmount();
                    inv.setItem(i, null);
                } else {
                    removed += canRemove;
                    stack.setAmount(stack.getAmount() - canRemove);
                }
            }
        }

        return CommandResult.success(removed);
    }

    private static CommandResult handleExperience(JsonObject params) {
        if (!params.has("username") || !params.has("action") || !params.has("amount")) {
            return CommandResult.error("Missing parameters: username, action, amount");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String action = params.get("action").getAsString().toLowerCase();
        int amount = params.get("amount").getAsInt();
        String target = params.has("target") ? params.get("target").getAsString().toLowerCase() : "points";

        try {
            Map<String, Object> result = new HashMap<>();

            switch (action) {
                case "add":
                    if ("levels".equals(target)) {
                        player.giveExpLevels(amount);
                    } else {
                        player.giveExp(amount);
                    }
                    break;
                case "set":
                    if ("levels".equals(target)) {
                        player.setLevel(amount);
                    } else {
                        player.setExp(Math.min(1.0f, Math.max(0.0f, (float) amount / player.getExpToLevel())));
                    }
                    break;
                case "query":
                    // just return current values
                    break;
                default:
                    return CommandResult.error("Invalid action: " + action + " (valid: add, set, query)");
            }

            result.put("level", player.getLevel());
            result.put("points", player.getTotalExperience());
            result.put("progress", player.getExp());
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to modify experience: " + e.getMessage());
        }
    }

    private static CommandResult handleDifficulty(JsonObject params) {
        if (!params.has("level")) {
            return CommandResult.error("Missing parameter: level");
        }

        String levelStr = params.get("level").getAsString().toUpperCase();
        org.bukkit.Difficulty difficulty;
        try {
            difficulty = org.bukkit.Difficulty.valueOf(levelStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid difficulty: " + levelStr +
                " (valid: PEACEFUL, EASY, NORMAL, HARD)");
        }

        World world = Bukkit.getWorlds().get(0);
        world.setDifficulty(difficulty);
        return CommandResult.success(true);
    }

    private static CommandResult handleGamerule(JsonObject params) {
        if (!params.has("rule")) {
            return CommandResult.error("Missing parameter: rule");
        }

        String ruleName = params.get("rule").getAsString();
        World world = Bukkit.getWorlds().get(0);

        org.bukkit.GameRule<?> gameRule = org.bukkit.GameRule.getByName(ruleName);
        if (gameRule == null) {
            return CommandResult.error("Invalid game rule: " + ruleName);
        }

        if (params.has("value") && !params.get("value").isJsonNull()) {
            String valueStr = params.get("value").getAsString();
            try {
                if (gameRule.getType() == Boolean.class) {
                    @SuppressWarnings("unchecked")
                    org.bukkit.GameRule<Boolean> boolRule = (org.bukkit.GameRule<Boolean>) gameRule;
                    world.setGameRule(boolRule, Boolean.parseBoolean(valueStr));
                } else if (gameRule.getType() == Integer.class) {
                    @SuppressWarnings("unchecked")
                    org.bukkit.GameRule<Integer> intRule = (org.bukkit.GameRule<Integer>) gameRule;
                    world.setGameRule(intRule, Integer.parseInt(valueStr));
                }
            } catch (Exception e) {
                return CommandResult.error("Failed to set game rule: " + e.getMessage());
            }
        }

        // Return current value
        Object currentValue = world.getGameRuleValue(gameRule);
        return CommandResult.success(currentValue != null ? currentValue.toString() : null);
    }

    // ===== Phase 2: Chat, display & sound =====
    private static CommandResult handleSay(JsonObject params) {
        if (!params.has("message")) {
            return CommandResult.error("Missing parameter: message");
        }

        String message = params.get("message").getAsString();
        Bukkit.broadcastMessage("[Server] " + message);
        return CommandResult.success(true);
    }

    private static CommandResult handleTell(JsonObject params) {
        if (!params.has("username") || !params.has("message")) {
            return CommandResult.error("Missing parameters: username, message");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String message = params.get("message").getAsString();
        player.sendMessage(message);
        return CommandResult.success(true);
    }

    private static CommandResult handleTellraw(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("username") || !params.has("json_text")) {
            return CommandResult.error("Missing parameters: username, json_text");
        }

        String username = params.get("username").getAsString();
        String jsonText = params.get("json_text").getAsString();

        try {
            final String cmd = "tellraw " + username + " " + jsonText;
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to send tellraw: " + e.getMessage());
        }
    }

    private static CommandResult handleTitle(JsonObject params) {
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String titleText = params.has("title") ? params.get("title").getAsString() : "";
        String subtitleText = params.has("subtitle") ? params.get("subtitle").getAsString() : "";
        int fadeIn = params.has("fade_in") ? params.get("fade_in").getAsInt() : 10;
        int stay = params.has("stay") ? params.get("stay").getAsInt() : 70;
        int fadeOut = params.has("fade_out") ? params.get("fade_out").getAsInt() : 20;

        try {
            player.sendTitle(titleText, subtitleText, fadeIn, stay, fadeOut);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to send title: " + e.getMessage());
        }
    }

    private static CommandResult handlePlaysound(JsonObject params) {
        if (!params.has("username") || !params.has("sound")) {
            return CommandResult.error("Missing parameters: username, sound");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String soundName = params.get("sound").getAsString();
        if (soundName.startsWith("minecraft:")) {
            soundName = soundName.substring(10);
        }

        String sourceStr = params.has("source") ? params.get("source").getAsString().toUpperCase() : "MASTER";
        org.bukkit.SoundCategory category;
        try {
            category = org.bukkit.SoundCategory.valueOf(sourceStr);
        } catch (IllegalArgumentException e) {
            category = org.bukkit.SoundCategory.MASTER;
        }

        Location loc;
        if (params.has("x") && params.has("y") && params.has("z")) {
            loc = new Location(player.getWorld(),
                params.get("x").getAsDouble(),
                params.get("y").getAsDouble(),
                params.get("z").getAsDouble());
        } else {
            loc = player.getLocation();
        }

        float volume = params.has("volume") ? params.get("volume").getAsFloat() : 1.0f;
        float pitch = params.has("pitch") ? params.get("pitch").getAsFloat() : 1.0f;

        try {
            player.playSound(loc, soundName, category, volume, pitch);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to play sound: " + e.getMessage());
        }
    }

    private static CommandResult handleStopsound(JsonObject params) {
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        try {
            if (params.has("sound") && !params.get("sound").isJsonNull()) {
                String soundName = params.get("sound").getAsString();
                if (soundName.startsWith("minecraft:")) {
                    soundName = soundName.substring(10);
                }

                if (params.has("source") && !params.get("source").isJsonNull()) {
                    String sourceStr = params.get("source").getAsString().toUpperCase();
                    org.bukkit.SoundCategory category = org.bukkit.SoundCategory.valueOf(sourceStr);
                    player.stopSound(soundName, category);
                } else {
                    player.stopSound(soundName);
                }
            } else {
                // Stop all sounds - use stopAllSounds if available, else stop empty string
                player.stopAllSounds();
            }
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to stop sound: " + e.getMessage());
        }
    }

    // ===== Phase 3: Particle, spawn & world settings =====
    private static CommandResult handleParticle(JsonObject params) {
        if (!params.has("particle") || !params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: particle, x, y, z");
        }

        String particleName = params.get("particle").getAsString();
        if (particleName.startsWith("minecraft:")) {
            particleName = particleName.substring(10);
        }

        org.bukkit.Particle particleType;
        try {
            particleType = org.bukkit.Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid particle type: " + particleName);
        }

        double x = params.get("x").getAsDouble();
        double y = params.get("y").getAsDouble();
        double z = params.get("z").getAsDouble();
        int count = params.has("count") ? params.get("count").getAsInt() : 1;
        double dx = params.has("dx") ? params.get("dx").getAsDouble() : 0;
        double dy = params.has("dy") ? params.get("dy").getAsDouble() : 0;
        double dz = params.has("dz") ? params.get("dz").getAsDouble() : 0;
        double speed = params.has("speed") ? params.get("speed").getAsDouble() : 0;

        World world = Bukkit.getWorlds().get(0);

        try {
            world.spawnParticle(particleType, x, y, z, count, dx, dy, dz, speed);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to spawn particle: " + e.getMessage());
        }
    }

    private static CommandResult handleSpawnpoint(JsonObject params) {
        if (!params.has("username")) {
            return CommandResult.error("Missing parameter: username");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        Location loc;
        if (params.has("x") && params.has("y") && params.has("z")) {
            loc = new Location(player.getWorld(),
                params.get("x").getAsDouble(),
                params.get("y").getAsDouble(),
                params.get("z").getAsDouble());
        } else {
            loc = player.getLocation();
        }

        try {
            player.setBedSpawnLocation(loc, true);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set spawnpoint: " + e.getMessage());
        }
    }

    private static CommandResult handleSetWorldSpawn(JsonObject params) {
        if (!params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: x, y, z");
        }

        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();

        World world = Bukkit.getWorlds().get(0);
        boolean success = world.setSpawnLocation(x, y, z);
        return CommandResult.success(success);
    }

    private static CommandResult handleWorldBorder(JsonObject params) {
        if (!params.has("action")) {
            return CommandResult.error("Missing parameter: action");
        }

        String action = params.get("action").getAsString().toLowerCase();
        World world = Bukkit.getWorlds().get(0);
        org.bukkit.WorldBorder border = world.getWorldBorder();

        Map<String, Object> result = new HashMap<>();

        try {
            switch (action) {
                case "get":
                    result.put("size", border.getSize());
                    result.put("center_x", border.getCenter().getX());
                    result.put("center_z", border.getCenter().getZ());
                    result.put("damage_amount", border.getDamageAmount());
                    result.put("damage_buffer", border.getDamageBuffer());
                    result.put("warning_distance", border.getWarningDistance());
                    result.put("warning_time", border.getWarningTime());
                    break;
                case "set":
                    if (!params.has("value")) {
                        return CommandResult.error("Missing parameter: value (size)");
                    }
                    double size = params.get("value").getAsDouble();
                    if (params.has("time") && params.get("time").getAsLong() > 0) {
                        long time = params.get("time").getAsLong();
                        border.setSize(size, time);
                    } else {
                        border.setSize(size);
                    }
                    result.put("size", size);
                    break;
                case "center":
                    if (!params.has("x") || !params.has("z")) {
                        return CommandResult.error("Missing parameters: x, z");
                    }
                    double cx = params.get("x").getAsDouble();
                    double cz = params.get("z").getAsDouble();
                    border.setCenter(cx, cz);
                    result.put("center_x", cx);
                    result.put("center_z", cz);
                    break;
                case "add":
                    if (!params.has("value")) {
                        return CommandResult.error("Missing parameter: value");
                    }
                    double addSize = border.getSize() + params.get("value").getAsDouble();
                    if (params.has("time") && params.get("time").getAsLong() > 0) {
                        border.setSize(addSize, params.get("time").getAsLong());
                    } else {
                        border.setSize(addSize);
                    }
                    result.put("size", addSize);
                    break;
                case "damage":
                    if (params.has("value")) {
                        border.setDamageAmount(params.get("value").getAsDouble());
                    }
                    result.put("damage_amount", border.getDamageAmount());
                    break;
                case "warning":
                    if (params.has("value")) {
                        border.setWarningDistance(params.get("value").getAsInt());
                    }
                    if (params.has("time")) {
                        border.setWarningTime(params.get("time").getAsInt());
                    }
                    result.put("warning_distance", border.getWarningDistance());
                    result.put("warning_time", border.getWarningTime());
                    break;
                default:
                    return CommandResult.error("Invalid action: " + action +
                        " (valid: get, set, center, add, damage, warning)");
            }
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to modify world border: " + e.getMessage());
        }
    }

    private static CommandResult handleForceload(JsonObject params) {
        if (!params.has("action") || !params.has("x") || !params.has("z")) {
            return CommandResult.error("Missing parameters: action, x, z");
        }

        String action = params.get("action").getAsString().toLowerCase();
        int x = params.get("x").getAsInt();
        int z = params.get("z").getAsInt();

        World world = Bukkit.getWorlds().get(0);
        // Convert block coordinates to chunk coordinates
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        try {
            switch (action) {
                case "add":
                    world.setChunkForceLoaded(chunkX, chunkZ, true);
                    return CommandResult.success(true);
                case "remove":
                    world.setChunkForceLoaded(chunkX, chunkZ, false);
                    return CommandResult.success(true);
                case "query":
                    boolean isForced = world.isChunkForceLoaded(chunkX, chunkZ);
                    return CommandResult.success(isForced);
                default:
                    return CommandResult.error("Invalid action: " + action + " (valid: add, remove, query)");
            }
        } catch (Exception e) {
            return CommandResult.error("Failed to modify forceload: " + e.getMessage());
        }
    }

    private static CommandResult handleDamage(JsonObject params) {
        if (!params.has("uuid") || !params.has("amount")) {
            return CommandResult.error("Missing parameters: uuid, amount");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof LivingEntity)) {
            return CommandResult.error("Entity is not a LivingEntity: " + uuidStr);
        }

        double amount = params.get("amount").getAsDouble();
        LivingEntity living = (LivingEntity) entity;

        try {
            if (params.has("source_uuid") && !params.get("source_uuid").isJsonNull()) {
                String sourceUuidStr = params.get("source_uuid").getAsString();
                UUID sourceUuid = UUID.fromString(sourceUuidStr);
                Entity sourceEntity = findEntityByUUID(sourceUuid);
                if (sourceEntity != null) {
                    living.damage(amount, sourceEntity);
                } else {
                    living.damage(amount);
                }
            } else {
                living.damage(amount);
            }
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to damage entity: " + e.getMessage());
        }
    }

    // ===== Phase 4: Scoreboard, tags & teams =====
    private static CommandResult handleAddObjective(JsonObject params) {
        if (!params.has("name") || !params.has("criteria")) {
            return CommandResult.error("Missing parameters: name, criteria");
        }

        String name = params.get("name").getAsString();
        String criteria = params.get("criteria").getAsString();
        String displayName = params.has("display_name") ? params.get("display_name").getAsString() : name;

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            if (scoreboard.getObjective(name) != null) {
                return CommandResult.error("Objective already exists: " + name);
            }
            org.bukkit.scoreboard.Criteria criteriaObj = org.bukkit.scoreboard.Criteria.create(criteria);
            scoreboard.registerNewObjective(name, criteriaObj, displayName);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to add objective: " + e.getMessage());
        }
    }

    private static CommandResult handleRemoveObjective(JsonObject params) {
        if (!params.has("name")) {
            return CommandResult.error("Missing parameter: name");
        }

        String name = params.get("name").getAsString();

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective(name);
            if (objective == null) {
                return CommandResult.error("Objective not found: " + name);
            }
            objective.unregister();
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to remove objective: " + e.getMessage());
        }
    }

    private static CommandResult handleSetScore(JsonObject params) {
        if (!params.has("objective") || !params.has("player") || !params.has("score")) {
            return CommandResult.error("Missing parameters: objective, player, score");
        }

        String objectiveName = params.get("objective").getAsString();
        String playerName = params.get("player").getAsString();
        int score = params.get("score").getAsInt();

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) {
                return CommandResult.error("Objective not found: " + objectiveName);
            }
            objective.getScore(playerName).setScore(score);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set score: " + e.getMessage());
        }
    }

    private static CommandResult handleGetScore(JsonObject params) {
        if (!params.has("objective") || !params.has("player")) {
            return CommandResult.error("Missing parameters: objective, player");
        }

        String objectiveName = params.get("objective").getAsString();
        String playerName = params.get("player").getAsString();

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) {
                return CommandResult.error("Objective not found: " + objectiveName);
            }
            org.bukkit.scoreboard.Score scoreObj = objective.getScore(playerName);
            return CommandResult.success(scoreObj.getScore());
        } catch (Exception e) {
            return CommandResult.error("Failed to get score: " + e.getMessage());
        }
    }

    private static CommandResult handleSetDisplaySlot(JsonObject params) {
        if (!params.has("slot")) {
            return CommandResult.error("Missing parameter: slot");
        }

        String slotStr = params.get("slot").getAsString().toUpperCase();
        org.bukkit.scoreboard.DisplaySlot slot;
        try {
            slot = org.bukkit.scoreboard.DisplaySlot.valueOf(slotStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid display slot: " + slotStr +
                " (valid: BELOW_NAME, SIDEBAR, PLAYER_LIST)");
        }

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

            if (params.has("objective") && !params.get("objective").isJsonNull()) {
                String objectiveName = params.get("objective").getAsString();
                org.bukkit.scoreboard.Objective objective = scoreboard.getObjective(objectiveName);
                if (objective == null) {
                    return CommandResult.error("Objective not found: " + objectiveName);
                }
                objective.setDisplaySlot(slot);
            } else {
                scoreboard.clearSlot(slot);
            }
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set display slot: " + e.getMessage());
        }
    }

    private static CommandResult handleAddTag(JsonObject params) {
        if (!params.has("uuid") || !params.has("tag")) {
            return CommandResult.error("Missing parameters: uuid, tag");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        String tag = params.get("tag").getAsString();
        boolean added = entity.addScoreboardTag(tag);
        return CommandResult.success(added);
    }

    private static CommandResult handleRemoveTag(JsonObject params) {
        if (!params.has("uuid") || !params.has("tag")) {
            return CommandResult.error("Missing parameters: uuid, tag");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        String tag = params.get("tag").getAsString();
        boolean removed = entity.removeScoreboardTag(tag);
        return CommandResult.success(removed);
    }

    private static CommandResult handleGetTags(JsonObject params) {
        if (!params.has("uuid")) {
            return CommandResult.error("Missing parameter: uuid");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        return CommandResult.success(new ArrayList<>(entity.getScoreboardTags()));
    }

    private static CommandResult handleTeam(JsonObject params) {
        if (!params.has("action")) {
            return CommandResult.error("Missing parameter: action");
        }

        String action = params.get("action").getAsString().toLowerCase();
        org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        try {
            Map<String, Object> result = new HashMap<>();

            switch (action) {
                case "add": {
                    if (!params.has("name")) {
                        return CommandResult.error("Missing parameter: name");
                    }
                    String name = params.get("name").getAsString();
                    if (scoreboard.getTeam(name) != null) {
                        return CommandResult.error("Team already exists: " + name);
                    }
                    org.bukkit.scoreboard.Team team = scoreboard.registerNewTeam(name);
                    if (params.has("display_name")) {
                        team.setDisplayName(params.get("display_name").getAsString());
                    }
                    result.put("name", name);
                    break;
                }
                case "remove": {
                    if (!params.has("name")) {
                        return CommandResult.error("Missing parameter: name");
                    }
                    String name = params.get("name").getAsString();
                    org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
                    if (team == null) {
                        return CommandResult.error("Team not found: " + name);
                    }
                    team.unregister();
                    result.put("removed", true);
                    break;
                }
                case "join": {
                    if (!params.has("name") || !params.has("members")) {
                        return CommandResult.error("Missing parameters: name, members");
                    }
                    String name = params.get("name").getAsString();
                    org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
                    if (team == null) {
                        return CommandResult.error("Team not found: " + name);
                    }
                    JsonArray members = params.getAsJsonArray("members");
                    for (JsonElement member : members) {
                        team.addEntry(member.getAsString());
                    }
                    result.put("added", members.size());
                    break;
                }
                case "leave": {
                    if (!params.has("name") || !params.has("members")) {
                        return CommandResult.error("Missing parameters: name, members");
                    }
                    String name = params.get("name").getAsString();
                    org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
                    if (team == null) {
                        return CommandResult.error("Team not found: " + name);
                    }
                    JsonArray members = params.getAsJsonArray("members");
                    for (JsonElement member : members) {
                        team.removeEntry(member.getAsString());
                    }
                    result.put("removed_count", members.size());
                    break;
                }
                case "modify": {
                    if (!params.has("name") || !params.has("option") || !params.has("value")) {
                        return CommandResult.error("Missing parameters: name, option, value");
                    }
                    String name = params.get("name").getAsString();
                    org.bukkit.scoreboard.Team team = scoreboard.getTeam(name);
                    if (team == null) {
                        return CommandResult.error("Team not found: " + name);
                    }
                    String option = params.get("option").getAsString().toLowerCase();
                    String value = params.get("value").getAsString();
                    switch (option) {
                        case "displayname":
                            team.setDisplayName(value);
                            break;
                        case "color":
                            team.setColor(org.bukkit.ChatColor.valueOf(value.toUpperCase()));
                            break;
                        case "friendlyfire":
                            team.setAllowFriendlyFire(Boolean.parseBoolean(value));
                            break;
                        case "seefriendlyinvisibles":
                            team.setCanSeeFriendlyInvisibles(Boolean.parseBoolean(value));
                            break;
                        case "prefix":
                            team.setPrefix(value);
                            break;
                        case "suffix":
                            team.setSuffix(value);
                            break;
                        default:
                            return CommandResult.error("Invalid option: " + option);
                    }
                    result.put("modified", true);
                    break;
                }
                case "list": {
                    List<Map<String, Object>> teams = new ArrayList<>();
                    for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
                        Map<String, Object> teamData = new HashMap<>();
                        teamData.put("name", team.getName());
                        teamData.put("display_name", team.getDisplayName());
                        teamData.put("members", new ArrayList<>(team.getEntries()));
                        teamData.put("size", team.getSize());
                        teams.add(teamData);
                    }
                    result.put("teams", teams);
                    break;
                }
                default:
                    return CommandResult.error("Invalid action: " + action +
                        " (valid: add, remove, join, leave, modify, list)");
            }
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to modify team: " + e.getMessage());
        }
    }

    private static final Map<String, org.bukkit.boss.BossBar> bossBars = new HashMap<>();

    private static CommandResult handleBossbar(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("action") || !params.has("id")) {
            return CommandResult.error("Missing parameters: action, id");
        }

        String action = params.get("action").getAsString().toLowerCase();
        String barId = params.get("id").getAsString();

        try {
            Map<String, Object> result = new HashMap<>();

            switch (action) {
                case "add": {
                    if (bossBars.containsKey(barId)) {
                        return CommandResult.error("BossBar already exists: " + barId);
                    }
                    String title = params.has("title") ? params.get("title").getAsString() : barId;
                    org.bukkit.boss.BarColor color = org.bukkit.boss.BarColor.WHITE;
                    if (params.has("color")) {
                        try {
                            color = org.bukkit.boss.BarColor.valueOf(params.get("color").getAsString().toUpperCase());
                        } catch (IllegalArgumentException ignored) {}
                    }
                    org.bukkit.boss.BarStyle style = org.bukkit.boss.BarStyle.SOLID;
                    if (params.has("style")) {
                        try {
                            style = org.bukkit.boss.BarStyle.valueOf(params.get("style").getAsString().toUpperCase());
                        } catch (IllegalArgumentException ignored) {}
                    }
                    org.bukkit.boss.BossBar bar = Bukkit.createBossBar(title, color, style);
                    if (params.has("progress")) {
                        bar.setProgress(Math.max(0.0, Math.min(1.0, params.get("progress").getAsDouble())));
                    }
                    if (params.has("visible")) {
                        bar.setVisible(params.get("visible").getAsBoolean());
                    }
                    bossBars.put(barId, bar);
                    result.put("id", barId);
                    break;
                }
                case "remove": {
                    org.bukkit.boss.BossBar bar = bossBars.remove(barId);
                    if (bar == null) {
                        return CommandResult.error("BossBar not found: " + barId);
                    }
                    bar.removeAll();
                    result.put("removed", true);
                    break;
                }
                case "set": {
                    org.bukkit.boss.BossBar bar = bossBars.get(barId);
                    if (bar == null) {
                        return CommandResult.error("BossBar not found: " + barId);
                    }
                    if (params.has("title")) {
                        bar.setTitle(params.get("title").getAsString());
                    }
                    if (params.has("color")) {
                        bar.setColor(org.bukkit.boss.BarColor.valueOf(params.get("color").getAsString().toUpperCase()));
                    }
                    if (params.has("style")) {
                        bar.setStyle(org.bukkit.boss.BarStyle.valueOf(params.get("style").getAsString().toUpperCase()));
                    }
                    if (params.has("progress")) {
                        bar.setProgress(Math.max(0.0, Math.min(1.0, params.get("progress").getAsDouble())));
                    }
                    if (params.has("visible")) {
                        bar.setVisible(params.get("visible").getAsBoolean());
                    }
                    result.put("updated", true);
                    break;
                }
                case "addplayer": {
                    org.bukkit.boss.BossBar bar = bossBars.get(barId);
                    if (bar == null) {
                        return CommandResult.error("BossBar not found: " + barId);
                    }
                    if (!params.has("username")) {
                        return CommandResult.error("Missing parameter: username");
                    }
                    Player player = Bukkit.getPlayer(params.get("username").getAsString());
                    if (player == null) {
                        return CommandResult.error("Player not found");
                    }
                    bar.addPlayer(player);
                    result.put("added", true);
                    break;
                }
                case "removeplayer": {
                    org.bukkit.boss.BossBar bar = bossBars.get(barId);
                    if (bar == null) {
                        return CommandResult.error("BossBar not found: " + barId);
                    }
                    if (!params.has("username")) {
                        return CommandResult.error("Missing parameter: username");
                    }
                    Player player = Bukkit.getPlayer(params.get("username").getAsString());
                    if (player == null) {
                        return CommandResult.error("Player not found");
                    }
                    bar.removePlayer(player);
                    result.put("removed", true);
                    break;
                }
                case "get": {
                    org.bukkit.boss.BossBar bar = bossBars.get(barId);
                    if (bar == null) {
                        return CommandResult.error("BossBar not found: " + barId);
                    }
                    result.put("id", barId);
                    result.put("title", bar.getTitle());
                    result.put("color", bar.getColor().name());
                    result.put("style", bar.getStyle().name());
                    result.put("progress", bar.getProgress());
                    result.put("visible", bar.isVisible());
                    List<String> playerNames = new ArrayList<>();
                    for (Player p : bar.getPlayers()) {
                        playerNames.add(p.getName());
                    }
                    result.put("players", playerNames);
                    break;
                }
                case "list": {
                    List<String> ids = new ArrayList<>(bossBars.keySet());
                    result.put("bars", ids);
                    break;
                }
                default:
                    return CommandResult.error("Invalid action: " + action +
                        " (valid: add, remove, set, addplayer, removeplayer, get, list)");
            }
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to modify bossbar: " + e.getMessage());
        }
    }

    private static CommandResult handleAttribute(JsonObject params) {
        if (!params.has("uuid") || !params.has("attribute")) {
            return CommandResult.error("Missing parameters: uuid, attribute");
        }

        String uuidStr = params.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + uuidStr);
        }

        Entity entity = findEntityByUUID(uuid);
        if (entity == null) {
            return CommandResult.error("Entity not found: " + uuidStr);
        }

        if (!(entity instanceof LivingEntity)) {
            return CommandResult.error("Entity is not a LivingEntity: " + uuidStr);
        }

        String attrName = params.get("attribute").getAsString().toUpperCase();
        // Handle both old (GENERIC_MAX_HEALTH) and new (MAX_HEALTH) naming
        Attribute attribute;
        try {
            attribute = Attribute.valueOf(attrName);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid attribute: " + attrName);
        }

        LivingEntity living = (LivingEntity) entity;
        org.bukkit.attribute.AttributeInstance attrInstance = living.getAttribute(attribute);
        if (attrInstance == null) {
            return CommandResult.error("Entity does not have attribute: " + attrName);
        }

        String attrAction = params.has("action") ? params.get("action").getAsString().toLowerCase() : "get";

        try {
            Map<String, Object> result = new HashMap<>();
            switch (attrAction) {
                case "get":
                    result.put("base_value", attrInstance.getBaseValue());
                    result.put("value", attrInstance.getValue());
                    break;
                case "set":
                    if (!params.has("value")) {
                        return CommandResult.error("Missing parameter: value");
                    }
                    attrInstance.setBaseValue(params.get("value").getAsDouble());
                    result.put("base_value", attrInstance.getBaseValue());
                    result.put("value", attrInstance.getValue());
                    break;
                default:
                    return CommandResult.error("Invalid action: " + attrAction + " (valid: get, set)");
            }
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to modify attribute: " + e.getMessage());
        }
    }

    // ===== Phase 5: Enchant, item & exploration =====
    private static CommandResult handleEnchant(JsonObject params) {
        if (!params.has("username") || !params.has("enchantment")) {
            return CommandResult.error("Missing parameters: username, enchantment");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            return CommandResult.error("Player is not holding an item");
        }

        String enchantName = params.get("enchantment").getAsString();
        if (enchantName.startsWith("minecraft:")) {
            enchantName = enchantName.substring(10);
        }

        org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByName(enchantName.toUpperCase());
        if (enchantment == null) {
            // Try by key
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(enchantName.toLowerCase());
            enchantment = org.bukkit.enchantments.Enchantment.getByKey(key);
        }
        if (enchantment == null) {
            return CommandResult.error("Invalid enchantment: " + enchantName);
        }

        int level = params.has("level") ? params.get("level").getAsInt() : 1;

        try {
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return CommandResult.error("Cannot enchant this item");
            }
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to enchant: " + e.getMessage());
        }
    }

    private static CommandResult handleGetItem(JsonObject params) {
        if (!params.has("username") || !params.has("slot")) {
            return CommandResult.error("Missing parameters: username, slot");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        int slot = params.get("slot").getAsInt();
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        if (slot < 0 || slot >= inv.getSize()) {
            return CommandResult.error("Invalid slot: " + slot + " (valid: 0-" + (inv.getSize() - 1) + ")");
        }

        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            Map<String, Object> result = new HashMap<>();
            result.put("item", null);
            result.put("amount", 0);
            return CommandResult.success(result);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("item", "minecraft:" + item.getType().name().toLowerCase());
        result.put("amount", item.getAmount());

        // Include enchantments
        if (item.getItemMeta() != null && !item.getItemMeta().getEnchants().isEmpty()) {
            Map<String, Integer> enchants = new HashMap<>();
            for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                enchants.put(entry.getKey().getKey().getKey(), entry.getValue());
            }
            result.put("enchantments", enchants);
        }

        return CommandResult.success(result);
    }

    private static CommandResult handleSetItem(JsonObject params) {
        if (!params.has("username") || !params.has("slot") || !params.has("item")) {
            return CommandResult.error("Missing parameters: username, slot, item");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        int slot = params.get("slot").getAsInt();
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        if (slot < 0 || slot >= inv.getSize()) {
            return CommandResult.error("Invalid slot: " + slot + " (valid: 0-" + (inv.getSize() - 1) + ")");
        }

        String itemName = params.get("item").getAsString();
        Material material = parseMaterial(itemName);
        if (material == null) {
            return CommandResult.error("Invalid item type: " + itemName);
        }

        int amount = params.has("amount") ? params.get("amount").getAsInt() : 1;

        try {
            ItemStack item = new ItemStack(material, amount);
            inv.setItem(slot, item);
            return CommandResult.success(true);
        } catch (Exception e) {
            return CommandResult.error("Failed to set item: " + e.getMessage());
        }
    }

    private static CommandResult handleLocate(JsonObject params) {
        if (!params.has("structure")) {
            return CommandResult.error("Missing parameter: structure");
        }

        String structureName = params.get("structure").getAsString();
        World world = Bukkit.getWorlds().get(0);

        Location searchFrom;
        if (params.has("x") && params.has("z")) {
            searchFrom = new Location(world, params.get("x").getAsDouble(), 64, params.get("z").getAsDouble());
        } else {
            searchFrom = world.getSpawnLocation();
        }

        try {
            org.bukkit.NamespacedKey key;
            if (structureName.contains(":")) {
                String[] parts = structureName.split(":", 2);
                key = new org.bukkit.NamespacedKey(parts[0], parts[1]);
            } else {
                key = org.bukkit.NamespacedKey.minecraft(structureName.toLowerCase());
            }
            org.bukkit.generator.structure.Structure structure = org.bukkit.Registry.STRUCTURE.get(key);
            if (structure == null) {
                return CommandResult.error("Invalid structure: " + structureName);
            }
            org.bukkit.util.StructureSearchResult searchResult = world.locateNearestStructure(searchFrom, structure, 100, false);
            if (searchResult == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("found", false);
                return CommandResult.success(result);
            }
            Location loc = searchResult.getLocation();
            Map<String, Object> result = new HashMap<>();
            result.put("found", true);
            result.put("x", loc.getBlockX());
            result.put("y", loc.getBlockY());
            result.put("z", loc.getBlockZ());
            return CommandResult.success(result);
        } catch (Exception e) {
            return CommandResult.error("Failed to locate structure: " + e.getMessage());
        }
    }

    private static CommandResult handleAdvancement(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("username") || !params.has("action") || !params.has("advancement")) {
            return CommandResult.error("Missing parameters: username, action, advancement");
        }

        String username = params.get("username").getAsString();
        Player player = Bukkit.getPlayer(username);
        if (player == null) {
            return CommandResult.error("Player not found: " + username);
        }

        String advancementName = params.get("advancement").getAsString();
        org.bukkit.NamespacedKey key;
        if (advancementName.contains(":")) {
            String[] parts = advancementName.split(":", 2);
            key = new org.bukkit.NamespacedKey(parts[0], parts[1]);
        } else {
            key = org.bukkit.NamespacedKey.minecraft(advancementName);
        }

        org.bukkit.advancement.Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) {
            return CommandResult.error("Invalid advancement: " + advancementName);
        }

        String action = params.get("action").getAsString().toLowerCase();
        org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(advancement);

        try {
            switch (action) {
                case "grant":
                    for (String criteria : progress.getRemainingCriteria()) {
                        progress.awardCriteria(criteria);
                    }
                    return CommandResult.success(true);
                case "revoke":
                    for (String criteria : progress.getAwardedCriteria()) {
                        progress.revokeCriteria(criteria);
                    }
                    return CommandResult.success(true);
                case "query":
                    return CommandResult.success(progress.isDone());
                default:
                    return CommandResult.error("Invalid action: " + action + " (valid: grant, revoke, query)");
            }
        } catch (Exception e) {
            return CommandResult.error("Failed to modify advancement: " + e.getMessage());
        }
    }

    private static CommandResult handleLoot(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("loot_table") || !params.has("x") || !params.has("y") || !params.has("z")) {
            return CommandResult.error("Missing parameters: loot_table, x, y, z");
        }

        String lootTableName = params.get("loot_table").getAsString();
        double x = params.get("x").getAsDouble();
        double y = params.get("y").getAsDouble();
        double z = params.get("z").getAsDouble();

        try {
            org.bukkit.NamespacedKey key;
            if (lootTableName.contains(":")) {
                String[] parts = lootTableName.split(":", 2);
                key = new org.bukkit.NamespacedKey(parts[0], parts[1]);
            } else {
                key = org.bukkit.NamespacedKey.minecraft(lootTableName);
            }
            org.bukkit.loot.LootTable lootTable = Bukkit.getLootTable(key);
            if (lootTable == null) {
                return CommandResult.error("Invalid loot table: " + lootTableName);
            }
            World world = Bukkit.getWorlds().get(0);
            Location loc = new Location(world, x, y, z);
            org.bukkit.loot.LootContext context = new org.bukkit.loot.LootContext.Builder(loc).build();
            java.util.Collection<ItemStack> items = lootTable.populateLoot(new java.util.Random(), context);
            for (ItemStack item : items) {
                world.dropItemNaturally(loc, item);
            }
            return CommandResult.success(items.size());
        } catch (Exception e) {
            return CommandResult.error("Failed to generate loot: " + e.getMessage());
        }
    }

    // ===== Phase 6: World generation & structure placement =====
    private static CommandResult handleFillBiome(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("x1") || !params.has("y1") || !params.has("z1") ||
            !params.has("x2") || !params.has("y2") || !params.has("z2") || !params.has("biome")) {
            return CommandResult.error("Missing parameters: x1, y1, z1, x2, y2, z2, biome");
        }

        int x1 = params.get("x1").getAsInt();
        int y1 = params.get("y1").getAsInt();
        int z1 = params.get("z1").getAsInt();
        int x2 = params.get("x2").getAsInt();
        int y2 = params.get("y2").getAsInt();
        int z2 = params.get("z2").getAsInt();
        String biome = params.get("biome").getAsString();

        try {
            StringBuilder cmd = new StringBuilder("fillbiome ");
            cmd.append(x1).append(" ").append(y1).append(" ").append(z1).append(" ");
            cmd.append(x2).append(" ").append(y2).append(" ").append(z2).append(" ");
            cmd.append(biome);
            if (params.has("filter_biome") && !params.get("filter_biome").isJsonNull()) {
                cmd.append(" replace ").append(params.get("filter_biome").getAsString());
            }
            final String cmdStr = cmd.toString();
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to fill biome: " + e.getMessage());
        }
    }

    private static CommandResult handlePlaceFeature(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("feature")) {
            return CommandResult.error("Missing parameter: feature");
        }

        String feature = params.get("feature").getAsString();
        StringBuilder cmd = new StringBuilder("place feature ").append(feature);
        if (params.has("x") && params.has("y") && params.has("z")) {
            cmd.append(" ").append(params.get("x").getAsInt());
            cmd.append(" ").append(params.get("y").getAsInt());
            cmd.append(" ").append(params.get("z").getAsInt());
        }

        try {
            final String cmdStr = cmd.toString();
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to place feature: " + e.getMessage());
        }
    }

    private static CommandResult handlePlaceStructure(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("structure")) {
            return CommandResult.error("Missing parameter: structure");
        }

        String structure = params.get("structure").getAsString();
        StringBuilder cmd = new StringBuilder("place structure ").append(structure);
        if (params.has("x") && params.has("y") && params.has("z")) {
            cmd.append(" ").append(params.get("x").getAsInt());
            cmd.append(" ").append(params.get("y").getAsInt());
            cmd.append(" ").append(params.get("z").getAsInt());
        }

        try {
            final String cmdStr = cmd.toString();
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to place structure: " + e.getMessage());
        }
    }

    private static CommandResult handlePlaceJigsaw(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("pool") || !params.has("target") || !params.has("max_depth")) {
            return CommandResult.error("Missing parameters: pool, target, max_depth");
        }

        String pool = params.get("pool").getAsString();
        String target = params.get("target").getAsString();
        int maxDepth = params.get("max_depth").getAsInt();

        StringBuilder cmd = new StringBuilder("place jigsaw ");
        cmd.append(pool).append(" ").append(target).append(" ").append(maxDepth);
        if (params.has("x") && params.has("y") && params.has("z")) {
            cmd.append(" ").append(params.get("x").getAsInt());
            cmd.append(" ").append(params.get("y").getAsInt());
            cmd.append(" ").append(params.get("z").getAsInt());
        }

        try {
            final String cmdStr = cmd.toString();
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to place jigsaw: " + e.getMessage());
        }
    }

    private static CommandResult handlePlaceTemplate(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("template")) {
            return CommandResult.error("Missing parameter: template");
        }

        String template = params.get("template").getAsString();
        StringBuilder cmd = new StringBuilder("place template ").append(template);
        if (params.has("x") && params.has("y") && params.has("z")) {
            cmd.append(" ").append(params.get("x").getAsInt());
            cmd.append(" ").append(params.get("y").getAsInt());
            cmd.append(" ").append(params.get("z").getAsInt());
        }
        if (params.has("rotation")) {
            cmd.append(" ").append(params.get("rotation").getAsString());
        }
        if (params.has("mirror")) {
            cmd.append(" ").append(params.get("mirror").getAsString());
        }
        if (params.has("integrity")) {
            cmd.append(" ").append(params.get("integrity").getAsFloat());
        }
        if (params.has("seed")) {
            cmd.append(" ").append(params.get("seed").getAsLong());
        }

        try {
            final String cmdStr = cmd.toString();
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdStr)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to place template: " + e.getMessage());
        }
    }

    // ===== Phase 7: Other utility commands =====
    private static CommandResult handleRide(JsonObject params) {
        if (!params.has("passenger_uuid")) {
            return CommandResult.error("Missing parameter: passenger_uuid");
        }

        String passengerUuidStr = params.get("passenger_uuid").getAsString();
        UUID passengerUuid;
        try {
            passengerUuid = UUID.fromString(passengerUuidStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid UUID format: " + passengerUuidStr);
        }

        Entity passenger = findEntityByUUID(passengerUuid);
        if (passenger == null) {
            return CommandResult.error("Passenger entity not found: " + passengerUuidStr);
        }

        if (params.has("vehicle_uuid") && !params.get("vehicle_uuid").isJsonNull()) {
            String vehicleUuidStr = params.get("vehicle_uuid").getAsString();
            UUID vehicleUuid;
            try {
                vehicleUuid = UUID.fromString(vehicleUuidStr);
            } catch (IllegalArgumentException e) {
                return CommandResult.error("Invalid vehicle UUID format: " + vehicleUuidStr);
            }

            Entity vehicle = findEntityByUUID(vehicleUuid);
            if (vehicle == null) {
                return CommandResult.error("Vehicle entity not found: " + vehicleUuidStr);
            }

            try {
                boolean success = vehicle.addPassenger(passenger);
                return CommandResult.success(success);
            } catch (Exception e) {
                return CommandResult.error("Failed to add passenger: " + e.getMessage());
            }
        } else {
            // Dismount
            try {
                boolean success = passenger.leaveVehicle();
                return CommandResult.success(success);
            } catch (Exception e) {
                return CommandResult.error("Failed to dismount: " + e.getMessage());
            }
        }
    }

    private static CommandResult handleSpreadplayers(MCPyLibPlugin plugin, JsonObject params) {
        if (!params.has("center_x") || !params.has("center_z") ||
            !params.has("spread_distance") || !params.has("max_range") ||
            !params.has("usernames")) {
            return CommandResult.error("Missing parameters: center_x, center_z, spread_distance, max_range, usernames");
        }

        double centerX = params.get("center_x").getAsDouble();
        double centerZ = params.get("center_z").getAsDouble();
        double spreadDistance = params.get("spread_distance").getAsDouble();
        double maxRange = params.get("max_range").getAsDouble();
        JsonArray usernames = params.getAsJsonArray("usernames");

        StringBuilder targets = new StringBuilder();
        for (int i = 0; i < usernames.size(); i++) {
            if (i > 0) targets.append(" ");
            targets.append(usernames.get(i).getAsString());
        }

        try {
            final String cmd = "spreadplayers " + centerX + " " + centerZ + " " +
                spreadDistance + " " + maxRange + " false " + targets;
            boolean success = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            ).get();
            return CommandResult.success(success);
        } catch (Exception e) {
            return CommandResult.error("Failed to spread players: " + e.getMessage());
        }
    }

    private static CommandResult handleDefaultGamemode(JsonObject params) {
        if (!params.has("mode")) {
            return CommandResult.error("Missing parameter: mode");
        }

        String modeStr = params.get("mode").getAsString().toUpperCase();
        GameMode gameMode;
        try {
            gameMode = GameMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            return CommandResult.error("Invalid gamemode: " + modeStr +
                " (valid: SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)");
        }

        Bukkit.setDefaultGameMode(gameMode);
        return CommandResult.success(true);
    }

    private static CommandResult handleList(JsonObject params) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> players = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("uuid", player.getUniqueId().toString());
            playerData.put("gamemode", player.getGameMode().name());
            players.add(playerData);
        }

        result.put("online_count", players.size());
        result.put("max_players", Bukkit.getMaxPlayers());
        result.put("players", players);
        return CommandResult.success(result);
    }
}
