package com.mcpylib.plugin;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            return CommandResult.success(1);
        } catch (Exception e) {
            return CommandResult.error("Failed to set block: " + e.getMessage());
        }
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
}
