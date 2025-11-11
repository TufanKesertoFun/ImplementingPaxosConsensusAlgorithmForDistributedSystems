package au.edu.adelaide.paxos.transport;

import java.util.function.Consumer;

public interface TransportServer extends AutoCloseable {

    void start(Consumer<String> onLine);
}
