package au.edu.adelaide.paxos.core;

/**
 * Composite proposal number {@code (counter, proposerId)} used by Paxos.
 * Implements {@link Comparable} for total ordering.
 *
 * @param counter    monotonically increasing local counter
 * @param proposerId numeric identifier of the proposer (e.g. 4 for "M4")
 */
public record ProposalNumber(int counter, int proposerId)
        implements Comparable<ProposalNumber> {

    /**
     * Parses a proposal number from the string representation "counter.proposerId".
     *
     * @param s encoded proposal number
     * @return new {@link ProposalNumber} instance
     */
    public static ProposalNumber parse(String s) {
        var p = s.split("\\.");
        return new ProposalNumber(
                Integer.parseInt(p[0]),
                Integer.parseInt(p[1])
        );
    }

    @Override
    public String toString() {
        return counter + "." + proposerId;
    }

    @Override
    public int compareTo(ProposalNumber o) {
        int c = Integer.compare(this.counter, o.counter);
        return (c != 0) ? c : Integer.compare(this.proposerId, o.proposerId);
    }
}
