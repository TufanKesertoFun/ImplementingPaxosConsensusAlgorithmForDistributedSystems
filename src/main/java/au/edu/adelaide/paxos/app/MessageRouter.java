package au.edu.adelaide.paxos.app;

import au.edu.adelaide.paxos.core.*;
import au.edu.adelaide.paxos.profile.NetworkProfile;
import au.edu.adelaide.paxos.roles.*;
import au.edu.adelaide.paxos.roles.impl.DefaultProposer;
import au.edu.adelaide.paxos.transport.TransportClient;
import au.edu.adelaide.paxos.util.Log;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes incoming text protocol lines to the correct Paxos role
 * (Proposer, Acceptor, Learner) and handles DECIDE broadcast logic.
 */
public final class MessageRouter {

    private final MemberId me;
    private final NetworkProfile profile;
    private final Acceptor acceptor;
    private final Learner learner;
    private final DefaultProposer proposer; // notify proposer on PROMISE/ACCEPTED/NACK
    private final TransportClient net;
    private final Log log;

    private final Set<String> decidedBroadcasted = ConcurrentHashMap.newKeySet();
    private final Set<String> decideSeen = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new message router for this member.
     *
     * @param me       this member's identifier
     * @param profile  network profile controlling delay and drops
     * @param acceptor local acceptor role
     * @param learner  local learner role
     * @param proposer local proposer used for callbacks
     * @param net      transport client for unicast/broadcast
     * @param log      logger for human-readable tracing
     */
    public MessageRouter(MemberId me,
                         NetworkProfile profile,
                         Acceptor acceptor,
                         Learner learner,
                         DefaultProposer proposer,
                         TransportClient net,
                         Log log) {
        this.me = me;
        this.profile = profile;
        this.acceptor = acceptor;
        this.learner = learner;
        this.proposer = proposer;
        this.net = net;
        this.log = log;
    }

    /**
     * Extracts a {@code key=value} pair from a semicolon-separated payload string.
     *
     * @param payload raw payload (e.g. {@code "na=1.4;va=M5"})
     * @param key     key to look for
     * @return trimmed value or empty string if not present
     */
    private static String kv(String payload, String key) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        for (String pair : payload.split(";")) {
            String[] p = pair.split("=", 2);
            if (p.length == 2 && p[0].trim().equals(key)) {
                return p[1].trim();
            }
        }
        return "";
    }

    /**
     * Handles a single decoded line of text protocol from the transport layer.
     * Applies network profile (drop/delay) and dispatches to appropriate role.
     *
     * @param line serialized message line (without trailing newline)
     */
    @SuppressWarnings("DuplicatedCode")
    public void onLine(String line) {
        if (profile.shouldDrop()) {
            return;
        }
        profile.maybeDelay();

        var m = Message.decode(line);
        switch (m.type()) {
            case PREPARE -> {
                // PREPARE|from|to|n|
                log.info("PREPARE from " + m.from().value() + " n=" + m.proposalNumber());
                var reply = acceptor.onPrepare(m);
                net.send(m.from(), reply);
            }

            case PROMISE -> {
                // PROMISE|from|to|n|na=<n_a>;va=<v_a>
                var n = m.proposalNumber(); // the PREPARE round this promise refers to
                var na = kv(m.value(), "na");
                var va = kv(m.value(), "va");
                log.info("PROMISE from " + m.from().value()
                        + " for n=" + n
                        + "  n_a=" + na
                        + "  v_a=" + va);
                // Let proposer handle quorum/adoption; proposer parses na/va from payload
                proposer.onPromise(n, m.value());
            }

            case ACCEPT_REQUEST -> {
                // ACCEPT_REQUEST|from|to|n|v=<value>
                var v = kv(m.value(), "v");
                log.info("ACCEPT_REQUEST from " + m.from().value()
                        + " n=" + m.proposalNumber()
                        + "  v=" + v);
                var reply = acceptor.onAcceptRequest(m);
                net.send(m.from(), reply);
            }

            case ACCEPTED -> {
                // ACCEPTED|from|to|n|v=<value>
                var v = kv(m.value(), "v");
                log.info("ACCEPTED from " + m.from().value()
                        + " n=" + m.proposalNumber()
                        + "  v=" + v);

                var decided = learner.onAccepted(m);
                if (decided != null) {
                    String key = m.proposalNumber() + ":" + decided;
                    if (decidedBroadcasted.add(key)) {
                        log.info("CONSENSUS: " + decided + " has been elected Council President!");
                        // Tell everyone explicitly
                        net.broadcast(new Message(
                                MessageType.DECIDE, me, null, m.proposalNumber(), "v=" + decided
                        ));
                    }
                }
                proposer.onAccepted(m.proposalNumber());
            }

            case DECIDE -> {
                // DECIDE|from|to|n|v=<value>
                var v = kv(m.value(), "v");
                String key = m.proposalNumber() + ":" + v;
                if (decideSeen.add(key)) {
                    // Print the exact rubric-required line on every node
                    log.info("CONSENSUS: " + v + " has been elected Council President!");
                }
                // Optionally: nudge learner to settle immediately (no-op if already decided)
                // learner.onAccepted(new Message(MessageType.ACCEPTED, m.from(), m.to(), m.proposalNumber(), "v=" + v));
            }

            case NACK -> {
                // Either old style: proposalNumber carried n_p
                // Or new style: payload has np=<n_p>
                var np = kv(m.value(), "np");
                if (np.isBlank()) {
                    np = m.proposalNumber() == null ? "" : m.proposalNumber();
                }
                log.info("NACK from " + m.from().value() + "  n_p=" + np);
                proposer.onNack(np);
            }

            default -> {
                // No default action; all known types are covered above.
            }
        }
    }
}
