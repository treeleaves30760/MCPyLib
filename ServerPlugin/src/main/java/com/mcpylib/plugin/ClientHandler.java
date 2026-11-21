package com.mcpylib.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

public class ClientHandler implements Runnable {

    private final MCPyLibPlugin plugin;
    private final Socket socket;
    private final Gson gson;
    private final Runnable onClose;

    public ClientHandler(MCPyLibPlugin plugin, Socket socket, Runnable onClose) {
        this.plugin = plugin;
        this.socket = socket;
        this.gson = new Gson();
        this.onClose = onClose;
    }

    @Override
    public void run() {
        try {
            // Free the slot of abandoned persistent connections. Clients
            // reconnect transparently when they find the socket closed.
            int idleSeconds = plugin.getConfig().getInt("server.idle-timeout-seconds", 300);
            if (idleSeconds > 0) {
                socket.setSoTimeout(idleSeconds * 1000);
            }
        } catch (IOException ignored) {
            // socket already broken; the read below will fail and clean up
        }
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Persistent connection: keep reading commands until client disconnects
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                if (requestLine.trim().isEmpty()) {
                    continue;
                }

                // Parse request
                JsonObject request;
                try {
                    request = gson.fromJson(requestLine, JsonObject.class);
                } catch (Exception e) {
                    sendError(out, "Invalid JSON");
                    continue;
                }

                // Validate token
                String token = request.has("token") ? request.get("token").getAsString() : "";
                if (!plugin.getTokenManager().validateToken(token)) {
                    sendError(out, "Invalid token");
                    continue;
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
            }

        } catch (SocketTimeoutException e) {
            if (plugin.getConfig().getBoolean("logging.log-connections", true)) {
                plugin.getLogger().info("Closing idle connection: " + socket.getInetAddress().getHostAddress());
            }
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("logging.log-connections", true)) {
                plugin.getLogger().log(Level.INFO, "Client disconnected: " +
                    socket.getInetAddress().getHostAddress());
            }
        } finally {
            onClose.run();
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
