package au.edu.adelaide.paxos.roles;

import au.edu.adelaide.paxos.core.Message;

/**
 * Paxos acceptor role interface.
 */
public interface Acceptor {

    /**
     * Handles a PREPARE message and returns a PROMISE or NACK response.
     *
     * @param m incoming PREPARE message
     * @return response message
     */
    Message onPrepare(Message m);

    /**
     * Handles an ACCEPT_REQUEST message and returns ACCEPTED or NACK.
     *
     * @param m incoming ACCEPT_REQUEST message
     * @return response message
     */
    Message onAcceptRequest(Message m);
}
