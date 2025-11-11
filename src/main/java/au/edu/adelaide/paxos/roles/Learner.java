package au.edu.adelaide.paxos.roles;

import au.edu.adelaide.paxos.core.Message;

public interface Learner {
    String onAccepted(Message m);
}
