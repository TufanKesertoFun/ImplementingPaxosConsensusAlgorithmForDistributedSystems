package au.edu.adelaide.paxos.roles;

import au.edu.adelaide.paxos.core.Message;

/**
 * Paxos learner role interface.
 */
public interface Learner {

    /**
     * Processes an ACCEPTED message and returns the decided value once quorum is reached.
     *
     * @param m ACCEPTED message
     * @return decided value or {@code null} if not yet decided
     */
    String onAccepted(Message m);
}
