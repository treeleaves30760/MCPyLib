package com.mcpylib.plugin;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
