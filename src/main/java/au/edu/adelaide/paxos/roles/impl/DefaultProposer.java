package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;
import au.edu.adelaide.paxos.core.MessageType;
import au.edu.adelaide.paxos.core.ProposalNumber;
import au.edu.adelaide.paxos.roles.Proposer;
import au.edu.adelaide.paxos.transport.TransportClient;

/**
 * Single-decree Paxos proposer for the council president election.
 * Emits ACCEPT_REQUEST and "ACCEPTED quorum" logs at most once per round.
 */
public final class DefaultProposer implements Proposer {

    private final MemberId me;
    private final int members;
    private final TransportClient net;

    // local, monotonically increasing counter for n = (counter, proposerId)
    private int localCounter = 0;

    // Current round state
    private String curN;               // proposal number "counter.proposerId"
    private String proposedValue;      // the value we prefer to elect

    private int promiseTally = 0;
    private int acceptedTally = 0;

    // Highest previously-accepted proposal seen in PROMISEs
    private ProposalNumber highestNaSeen = null;
    private String adoptedValue = null;

    // Emit-once guards (per round)
    private boolean sentAcceptForCurN = false;
    private boolean announcedAcceptedForCurN = false;

    public DefaultProposer(MemberId me, int members, TransportClient net) {
        this.me = me;
        this.members = members;
        this.net = net;
    }

    private ProposalNumber nextN() {
        localCounter++;
        int proposerId = Integer.parseInt(me.value().substring(1)); // "M4" -> 4
        return new ProposalNumber(localCounter, proposerId);
    }

    private int quorum() { return members / 2 + 1; }

    @Override
    public synchronized void propose(String candidate) {
        // Fresh round state
        this.proposedValue = candidate;
        this.promiseTally = 0;
        this.acceptedTally = 0;
        this.highestNaSeen = null;
        this.adoptedValue  = null;
        this.sentAcceptForCurN = false;
        this.announcedAcceptedForCurN = false;

        var n = nextN();
        this.curN = n.toString();

        System.out.printf("[%s] [PROPOSER] PREPARE n=%s (want=%s)%n", me.value(), curN, proposedValue);
        net.broadcast(new Message(MessageType.PREPARE, me, null, curN, null));
    }

    /**
     * Called when a PROMISE arrives.
     * @param n_a acceptor's previously-accepted proposal number (may be null/empty)
     * @param v_a acceptor's previously-accepted value (may be null/empty)
     */
    public synchronized void onPromise(String n_a, String v_a) {
        // Only tally for our current round; older messages are harmlessly counted but guards prevent duplicates
        promiseTally++;

        // Track highest n_a to adopt its v_a if any accepted value exists
        if (n_a != null && !n_a.isBlank()) {
            var na = ProposalNumber.parse(n_a);
            if (highestNaSeen == null || na.compareTo(highestNaSeen) > 0) {
                highestNaSeen = na;
                adoptedValue  = (v_a == null || v_a.isBlank()) ? null : v_a;
            }
        }

        if (promiseTally >= quorum() && !sentAcceptForCurN) {
            var chosen = (adoptedValue != null) ? adoptedValue : proposedValue;
            System.out.printf("[%s] [PROPOSER] PROMISE quorum for n=%s -> ACCEPT_REQUEST(%s)%n",
                    me.value(), curN, chosen);
            net.broadcast(new Message(MessageType.ACCEPT_REQUEST, me, null, curN, chosen));
            sentAcceptForCurN = true; // emit once
        }
    }

    /** Called when an ACCEPTED arrives. */
    public synchronized void onAccepted(String n) {
        if (!n.equals(curN)) return; // ignore old/foreign rounds
        acceptedTally++;
        if (acceptedTally >= quorum() && !announcedAcceptedForCurN) {
            System.out.printf("[%s] [PROPOSER] ACCEPTED quorum for n=%s%n", me.value(), n);
            announcedAcceptedForCurN = true; // emit once
        }
    }

    /** Called when a NACK arrives; suggestedNp carries acceptor’s highest promised n_p. */
    public synchronized void onNack(String suggestedNp) {
        if (suggestedNp == null || suggestedNp.isBlank()) return;
        var np = ProposalNumber.parse(suggestedNp);

        int myId = Integer.parseInt(me.value().substring(1));
        // Ensure our next counter is strictly greater than n_p
        localCounter = Math.max(localCounter, np.counter()) + 1;

        // Restart same value with a higher n; reset per-round state
        this.promiseTally = 0;
        this.acceptedTally = 0;
        this.highestNaSeen = null;
        this.adoptedValue  = null;
        this.sentAcceptForCurN = false;
        this.announcedAcceptedForCurN = false;

        var n = new ProposalNumber(localCounter, myId);
        this.curN = n.toString();

        System.out.printf("[%s] [PROPOSER] NACK -> retry PREPARE with n=%s%n", me.value(), curN);
        net.broadcast(new Message(MessageType.PREPARE, me, null, curN, null));
    }
}
