package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.Message;
import au.edu.adelaide.paxos.roles.Learner;

import java.util.HashMap;
import java.util.Map;

/**
 * Default Paxos learner that counts ACCEPTED messages per (n, v) until quorum.
 */
public final class DefaultLearner implements Learner {

    private final int quorum;
    private final Map<String, Map<String, Integer>> counts = new HashMap<>();

    /**
     * Creates a learner with given number of members.
     *
     * @param members total number of participants in the cluster
     */
    public DefaultLearner(int members) {
        this.quorum = members / 2 + 1;
    }

    @Override
    public synchronized String onAccepted(Message m) {

        counts.computeIfAbsent(m.proposalNumber(), k -> new HashMap<>());

        var map = counts.get(m.proposalNumber());
        var newC = map.merge(m.value(), 1, Integer::sum);

        if (newC >= quorum) {
            return m.value();
        }
        return null;
    }
}
