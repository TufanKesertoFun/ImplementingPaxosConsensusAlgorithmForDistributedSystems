package au.edu.adelaide.paxos.transport;

import java.util.function.Consumer;

/**
 * Simple server interface that dispatches each received line to a consumer.
 */
public interface TransportServer extends AutoCloseable {

    /**
     * Starts the server and invokes the callback for each received line.
     *
     * @param onLine consumer called per text line
     */
    void start(Consumer<String> onLine);
}
