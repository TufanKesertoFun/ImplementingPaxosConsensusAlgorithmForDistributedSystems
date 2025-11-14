package au.edu.adelaide.paxos.roles;

import au.edu.adelaide.paxos.core.Message;

public interface Acceptor {
    Message onPrepare(Message m);

    Message onAcceptRequest(Message m);
}
