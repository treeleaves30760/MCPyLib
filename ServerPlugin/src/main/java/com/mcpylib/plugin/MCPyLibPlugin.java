package com.mcpylib.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class MCPyLibPlugin extends JavaPlugin {

    private NetworkServer networkServer;
    private TokenManager tokenManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize token manager
        tokenManager = new TokenManager(this);

        // Get configuration
        String host = getConfig().getString("server.host", "0.0.0.0");
        int port = getConfig().getInt("server.port", 65535);
        int maxConnections = getConfig().getInt("server.max-connections", 10);

        // Start network server
        try {
            networkServer = new NetworkServer(this, host, port, maxConnections);
            networkServer.start();

            getLogger().info("MCPyLib plugin enabled!");
            getLogger().info("Server listening on " + host + ":" + port);
            getLogger().info("Authentication token: " + tokenManager.getToken());

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start network server", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        getCommand("mcpylib").setExecutor(new MCPyLibCommand(this));
    }

    @Override
    public void onDisable() {
        // Stop network server
        if (networkServer != null) {
            networkServer.stop();
        }

        getLogger().info("MCPyLib plugin disabled!");
    }

    public NetworkServer getNetworkServer() {
        return networkServer;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }
}
