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

public class CommandHandler {

    public static CommandResult handleCommand(MCPyLibPlugin plugin, String action, JsonObject params) {
        try {
            switch (action.toLowerCase()) {
                case "setblock":
                    return handleSetBlock(params);
                case "getblock":
                    return handleGetBlock(params);
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
                        block.setType(material);
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
                            block.setType(material);
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
                            block.setType(material);

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
                status.put("max_health", living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
}
