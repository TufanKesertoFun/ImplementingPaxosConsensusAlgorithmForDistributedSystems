package au.edu.adelaide.paxos.transport;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;

/**
 * Abstraction of a client capable of sending messages to members or broadcasting.
 */
public interface TransportClient {

    /**
     * Sends a message to a single destination member.
     *
     * @param to destination member id
     * @param m  message to send
     */
    void send(MemberId to, Message m);

    /**
     * Broadcasts a message to all members except the sender.
     *
     * @param m message to broadcast
     */
    void broadcast(Message m);

}
