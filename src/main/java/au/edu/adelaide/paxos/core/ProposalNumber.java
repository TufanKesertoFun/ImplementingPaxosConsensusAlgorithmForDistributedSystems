package au.edu.adelaide.paxos.core;

public record ProposalNumber(int counter, int proposerId) implements
        Comparable<ProposalNumber> {
    public static ProposalNumber parse(String s) {
        var p = s.split("\\.");
        return new ProposalNumber(Integer.parseInt(p[0]),
                Integer.parseInt(p[1]));
    }
    @Override public String toString()
    {
        return counter + "." + proposerId;
    }
    @Override public int compareTo(ProposalNumber o){
        int c = Integer.compare(this.counter, o.counter);
        return (c != 0) ? c : Integer.compare(this.proposerId, o.proposerId);
    }
}
