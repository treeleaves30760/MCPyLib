package com.mcpylib.plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AtomicInteger activeConnections = new AtomicInteger();

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
        // Bind to the configured address (e.g. 127.0.0.1 to allow local clients only)
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
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

                // Reject instead of queueing forever when every slot is taken
                if (activeConnections.incrementAndGet() > maxConnections) {
                    activeConnections.decrementAndGet();
                    Thread.startVirtualThread(() -> rejectBusy(clientSocket));
                    continue;
                }

                // Handle connection in thread pool
                executorService.submit(new ClientHandler(plugin, clientSocket, activeConnections::decrementAndGet));

            } catch (IOException e) {
                if (running.get()) {
                    plugin.getLogger().log(Level.WARNING, "Error accepting connection", e);
                }
            }
        }
    }

    private void rejectBusy(Socket socket) {
        plugin.getLogger().warning("Rejected connection from " + socket.getInetAddress().getHostAddress()
                + ": max-connections (" + maxConnections + ") reached");
        try (socket) {
            // Read the client's request first so it receives a proper JSON error
            // response instead of a connection reset.
            socket.setSoTimeout(2000);
            new java.io.BufferedReader(new java.io.InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            OutputStream out = socket.getOutputStream();
            out.write(("{\"success\":false,\"error\":\"Server busy: max-connections (" + maxConnections
                    + ") reached. Close unused clients or raise server.max-connections.\",\"data\":null}\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ignored) {
            // client already gone
        }
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }
}
