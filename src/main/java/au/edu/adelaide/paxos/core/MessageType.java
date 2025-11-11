package au.edu.adelaide.paxos.core;

public enum MessageType {
    PREPARE,
    PROMISE,
    ACCEPT_REQUEST,
    ACCEPTED,
    DECIDE,
    NACK
}
