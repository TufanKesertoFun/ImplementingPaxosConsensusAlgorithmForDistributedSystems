package au.edu.adelaide.paxos.roles;

public interface Proposer {
    void propose(String candidate);
}
