package com.mcpylib.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MCPyLibCommand implements CommandExecutor {

    private final MCPyLibPlugin plugin;

    public MCPyLibCommand(MCPyLibPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "MCPyLib v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Usage: /mcpylib <reload|token|status>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "token":
                return handleToken(sender, args);
            case "status":
                return handleStatus(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                return false;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mcpylib.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
            return false;
        }
    }

    private boolean handleToken(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mcpylib.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("regenerate")) {
            plugin.getTokenManager().regenerateToken();
            sender.sendMessage(ChatColor.GREEN + "Token regenerated successfully!");
            sender.sendMessage(ChatColor.YELLOW + "New token: " + ChatColor.WHITE +
                plugin.getTokenManager().getToken());
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Current token: " + ChatColor.WHITE +
                plugin.getTokenManager().getToken());
            sender.sendMessage(ChatColor.GRAY + "Use '/mcpylib token regenerate' to generate a new token");
        }

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("mcpylib.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        NetworkServer server = plugin.getNetworkServer();
        sender.sendMessage(ChatColor.GOLD + "=== MCPyLib Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE +
            plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Server Status: " + ChatColor.WHITE +
            (server.isRunning() ? ChatColor.GREEN + "Running" : ChatColor.RED + "Stopped"));
        sender.sendMessage(ChatColor.YELLOW + "Port: " + ChatColor.WHITE + server.getPort());
        sender.sendMessage(ChatColor.YELLOW + "Token Authentication: " + ChatColor.WHITE +
            (plugin.getConfig().getBoolean("security.require-token", true) ?
                ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

        return true;
    }
}
