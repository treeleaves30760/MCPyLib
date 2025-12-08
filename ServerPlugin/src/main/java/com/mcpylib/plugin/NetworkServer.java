package com.mcpylib.plugin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class NetworkServer {

    private final MCPyLibPlugin plugin;
    private final String host;
    private final int port;
    private final int maxConnections;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Thread acceptThread;
    private AtomicBoolean running;

    public NetworkServer(MCPyLibPlugin plugin, String host, int port, int maxConnections) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.running = new AtomicBoolean(false);
    }

    public void start() throws IOException {
        if (running.get()) {
            return;
        }

        // Create server socket
        serverSocket = new ServerSocket(port);
        executorService = Executors.newFixedThreadPool(maxConnections);
        running.set(true);

        // Start accept thread
        acceptThread = new Thread(this::acceptConnections, "MCPyLib-Accept");
        acceptThread.start();

        plugin.getLogger().info("Network server started on port " + port);
    }

    public void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing server socket", e);
        }

        // Shutdown executor
        if (executorService != null) {
            executorService.shutdown();
        }

        // Wait for accept thread
        if (acceptThread != null) {
            try {
                acceptThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        plugin.getLogger().info("Network server stopped");
    }

    private void acceptConnections() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();

                // Log connection
                if (plugin.getConfig().getBoolean("logging.log-connections", true)) {
                    plugin.getLogger().info("New connection from " +
                        clientSocket.getInetAddress().getHostAddress());
                }

                // Handle connection in thread pool
                executorService.submit(new ClientHandler(plugin, clientSocket));

            } catch (IOException e) {
                if (running.get()) {
                    plugin.getLogger().log(Level.WARNING, "Error accepting connection", e);
                }
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }
}
