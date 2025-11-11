package au.edu.adelaide.paxos.transport;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;

public interface TransportClient {
    void send(MemberId to, Message m);
    void broadcast(Message m);

}
