package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;
import au.edu.adelaide.paxos.core.MessageType;
import au.edu.adelaide.paxos.core.ProposalNumber;
import au.edu.adelaide.paxos.roles.Proposer;
import au.edu.adelaide.paxos.transport.TransportClient;

/**
 * Single-decree Paxos proposer for the council president election.
 * Uses text protocol: proposalNumber = n, value = key=value pairs.
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

    /**
     * Creates a new proposer bound to a member id.
     *
     * @param me      current member id
     * @param members number of members in the cluster
     * @param net     transport client used to send and broadcast messages
     */
    public DefaultProposer(MemberId me, int members, TransportClient net) {
        this.me = me;
        this.members = members;
        this.net = net;
    }

    /**
     * Generates the next proposal number for this proposer.
     *
     * @return new {@link ProposalNumber}
     */
    private ProposalNumber nextN() {
        localCounter++;
        int proposerId = Integer.parseInt(me.value().substring(1)); // "M4" -> 4
        return new ProposalNumber(localCounter, proposerId);
    }

    /**
     * Calculates the quorum size based on the number of members.
     *
     * @return quorum threshold
     */
    private int quorum() {
        return members / 2 + 1;
    }

    @Override
    public synchronized void propose(String candidate) {
        // Fresh round state
        this.proposedValue = candidate;
        this.promiseTally = 0;
        this.acceptedTally = 0;
        this.highestNaSeen = null;
        this.adoptedValue = null;
        this.sentAcceptForCurN = false;
        this.announcedAcceptedForCurN = false;

        var n = nextN();
        this.curN = n.toString();

        System.out.printf("[%s] [PROPOSER] PREPARE n=%s (want=%s)%n", me.value(), curN, proposedValue);
        // PREPARE carries n, empty payload
        net.broadcast(new Message(MessageType.PREPARE, me, null, curN, ""));
    }

    /**
     * Handles a PROMISE message for a given PREPARE round.
     *
     * @param promisedN the PREPARE round this promise refers to (must equal curN to count)
     * @param payload   key=value pairs containing "na" and "va" (both may be empty)
     */
    public synchronized void onPromise(String promisedN, String payload) {
        // Only tally for our current round (critical for safety)
        if (promisedN == null || !promisedN.equals(curN)) {
            return;
        }

        promiseTally++;

        String naStr = kv(payload, "na");
        String vaStr = kv(payload, "va");
        if (!naStr.isBlank()) {
            var na = ProposalNumber.parse(naStr);
            if (highestNaSeen == null || na.compareTo(highestNaSeen) > 0) {
                highestNaSeen = na;
                adoptedValue = vaStr.isBlank() ? null : vaStr;
            }
        }

        if (promiseTally >= quorum() && !sentAcceptForCurN) {
            var chosen = (adoptedValue != null) ? adoptedValue : proposedValue;
            System.out.printf("[%s] [PROPOSER] PROMISE quorum for n=%s -> ACCEPT_REQUEST(%s)%n",
                    me.value(), curN, chosen);
            net.broadcast(new Message(
                    MessageType.ACCEPT_REQUEST, me, null, curN, "v=" + chosen
            ));
            sentAcceptForCurN = true; // emit once
        }
    }

    /**
     * Called when an ACCEPTED message arrives.
     *
     * @param n proposal number string this ACCEPTED refers to
     */
    public synchronized void onAccepted(String n) {
        if (!n.equals(curN)) {
            return; // ignore old/foreign rounds
        }
        acceptedTally++;
        if (acceptedTally >= quorum() && !announcedAcceptedForCurN) {
            System.out.printf("[%s] [PROPOSER] ACCEPTED quorum for n=%s%n", me.value(), n);
            announcedAcceptedForCurN = true; // emit once
        }
    }

    /**
     * Called when a NACK arrives; supports payload "np=..." or legacy raw n_p string.
     *
     * @param suggestedNpOrPayload encoded n_p value or payload containing np
     */
    public synchronized void onNack(String suggestedNpOrPayload) {
        String npStr = suggestedNpOrPayload;
        if (npStr != null && npStr.contains("=")) {
            npStr = kv(suggestedNpOrPayload, "np");
        }
        if (npStr == null || npStr.isBlank()) {
            return;
        }

        var np = ProposalNumber.parse(npStr);

        int myId = Integer.parseInt(me.value().substring(1));
        // Ensure our next counter is strictly greater than n_p
        localCounter = Math.max(localCounter, np.counter()) + 1;

        // Restart same value with a higher n; reset per-round state
        this.promiseTally = 0;
        this.acceptedTally = 0;
        this.highestNaSeen = null;
        this.adoptedValue = null;
        this.sentAcceptForCurN = false;
        this.announcedAcceptedForCurN = false;

        var n = new ProposalNumber(localCounter, myId);
        this.curN = n.toString();

        System.out.printf("[%s] [PROPOSER] NACK -> retry PREPARE with n=%s%n", me.value(), curN);
        net.broadcast(new Message(MessageType.PREPARE, me, null, curN, ""));
    }

    /**
     * Helper to parse a value from a semicolon-separated key=value payload.
     *
     * @param payload raw payload string
     * @param key     key to search for
     * @return extracted value or empty string
     */
    private static String kv(String payload, String key) {
        if (payload == null) {
            return "";
        }
        for (String kv : payload.split(";")) {
            String[] p = kv.split("=", 2);
            if (p.length == 2 && p[0].trim().equals(key)) {
                return p[1].trim();
            }
        }
        return "";
    }
}
