package com.mcpylib.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

public class ClientHandler implements Runnable {

    private final MCPyLibPlugin plugin;
    private final Socket socket;
    private final Gson gson;

    public ClientHandler(MCPyLibPlugin plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Read request
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                sendError(out, "Empty request");
                return;
            }

            // Parse request
            JsonObject request;
            try {
                request = gson.fromJson(requestLine, JsonObject.class);
            } catch (Exception e) {
                sendError(out, "Invalid JSON");
                return;
            }

            // Validate token
            String token = request.has("token") ? request.get("token").getAsString() : "";
            if (!plugin.getTokenManager().validateToken(token)) {
                sendError(out, "Invalid token");
                return;
            }

            // Get action and params
            String action = request.has("action") ? request.get("action").getAsString() : "";
            JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

            // Log command
            if (plugin.getConfig().getBoolean("logging.log-commands", true)) {
                plugin.getLogger().info("Executing command: " + action);
            }

            // Execute command on main thread and wait for result
            CommandResult result = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                return CommandHandler.handleCommand(plugin, action, params);
            }).get();

            // Send response
            if (result.isSuccess()) {
                sendSuccess(out, result.getData());
            } else {
                sendError(out, result.getError());
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error handling client", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing socket", e);
            }
        }
    }

    private void sendSuccess(PrintWriter out, Object data) {
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("data", gson.toJsonTree(data));
        out.println(gson.toJson(response));
    }

    private void sendError(PrintWriter out, String error) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("error", error);
        response.add("data", null);
        out.println(gson.toJson(response));
    }
}
