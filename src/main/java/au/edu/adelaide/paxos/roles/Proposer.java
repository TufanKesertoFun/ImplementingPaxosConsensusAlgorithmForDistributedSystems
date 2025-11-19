package au.edu.adelaide.paxos.roles;

/**
 * Paxos proposer role interface.
 */
public interface Proposer {

    /**
     * Initiates a new proposal for the given candidate value.
     *
     * @param candidate value to propose
     */
    void propose(String candidate);
}
