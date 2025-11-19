package au.edu.adelaide.paxos.core;

/**
 * High-level message types used in the Paxos protocol implementation.
 */
public enum MessageType {
    PREPARE,
    PROMISE,
    ACCEPT_REQUEST,
    ACCEPTED,
    DECIDE,
    NACK
}
