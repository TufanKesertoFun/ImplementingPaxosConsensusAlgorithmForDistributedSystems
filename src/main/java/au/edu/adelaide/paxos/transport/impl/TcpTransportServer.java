package au.edu.adelaide.paxos.transport.impl;

import au.edu.adelaide.paxos.transport.TransportServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Simple TCP server that accepts connections and forwards lines to a callback.
 */
public final class TcpTransportServer implements TransportServer {

    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket server;

    /**
     * Creates a TCP server on the given port.
     *
     * @param port listening port
     */
    public TcpTransportServer(int port) {
        this.port = port;
    }

    @Override
    public void start(Consumer<String> onLine) {
        pool.submit(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                server = ss;
                while (!ss.isClosed()) {
                    Socket s = ss.accept();
                    pool.submit(() -> handle(s, onLine));
                }
            } catch (IOException ignored) {
                // Intentionally ignored: server shut down or binding failure handled externally.
            }
        });
    }

    /**
     * Handles a single accepted socket, forwarding each line to the consumer.
     *
     * @param s      accepted socket
     * @param onLine consumer invoked for each received line
     */
    private void handle(Socket s, Consumer<String> onLine) {
        try (s; var r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                onLine.accept(line);
            }
        } catch (IOException ignored) {
            // Intentionally ignored: client disconnects or IO errors are expected.
        }
    }

    @Override
    public void close() {
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException ignored) {
            // Intentionally ignored: closing server socket best-effort.
        }
        pool.shutdownNow();
    }
}
