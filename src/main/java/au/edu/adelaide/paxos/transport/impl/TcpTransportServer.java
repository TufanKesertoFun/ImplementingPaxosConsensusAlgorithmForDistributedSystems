package au.edu.adelaide.paxos.transport.impl;

import au.edu.adelaide.paxos.transport.TransportServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class TcpTransportServer implements TransportServer {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket server;

    public TcpTransportServer(int port) { this.port = port; }

    @Override public void start(Consumer<String> onLine) {
        pool.submit(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                server = ss;
                while (!ss.isClosed()) {
                    Socket s = ss.accept();
                    pool.submit(() -> handle(s, onLine));
                }
            } catch (IOException ignored) { }
        });
    }

    private void handle(Socket s, Consumer<String> onLine) {
        try (s; var r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) onLine.accept(line);
        } catch (IOException ignored) { }
    }

    @Override public void close() {
        try { if (server != null) server.close(); } catch (IOException ignored) {}
        pool.shutdownNow();
    }
}
