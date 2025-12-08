package com.mcpylib.plugin;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenManager {

    private final MCPyLibPlugin plugin;
    private String token;

    public TokenManager(MCPyLibPlugin plugin) {
        this.plugin = plugin;
        loadToken();
    }

    private void loadToken() {
        // Get token from config
        token = plugin.getConfig().getString("security.token", "");

        // Generate new token if not exists
        if (token == null || token.isEmpty()) {
            token = generateToken();
            plugin.getConfig().set("security.token", token);
            plugin.saveConfig();
            plugin.getLogger().info("Generated new authentication token: " + token);
        }
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String getToken() {
        return token;
    }

    public boolean validateToken(String inputToken) {
        // Check if token authentication is required
        boolean requireToken = plugin.getConfig().getBoolean("security.require-token", true);
        if (!requireToken) {
            return true;
        }

        // Validate token
        return token != null && token.equals(inputToken);
    }

    public void regenerateToken() {
        token = generateToken();
        plugin.getConfig().set("security.token", token);
        plugin.saveConfig();
        plugin.getLogger().info("Regenerated authentication token: " + token);
    }
}
