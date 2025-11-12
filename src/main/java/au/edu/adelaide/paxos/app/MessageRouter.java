package au.edu.adelaide.paxos.app;

import au.edu.adelaide.paxos.core.*;
import au.edu.adelaide.paxos.profile.NetworkProfile;
import au.edu.adelaide.paxos.roles.*;
import au.edu.adelaide.paxos.roles.impl.DefaultProposer;
import au.edu.adelaide.paxos.transport.TransportClient;
import au.edu.adelaide.paxos.util.Log;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageRouter {
    private final MemberId me;
    private final NetworkProfile profile;
    private final Acceptor acceptor;
    private final Learner learner;
    private final DefaultProposer proposer; // so we can notify on PROMISE/ACCEPTED
    private final TransportClient net;
    private final Log log;

    private final Set<String> decidedBroadcasted = ConcurrentHashMap.newKeySet();
    private final Set<String> decideSeen = ConcurrentHashMap.newKeySet();

    public MessageRouter(MemberId me, NetworkProfile profile,
                         Acceptor acceptor, Learner learner,
                         DefaultProposer proposer, TransportClient net, Log log) {
        this.me = me; this.profile = profile; this.acceptor = acceptor;
        this.learner = learner; this.proposer = proposer; this.net = net; this.log = log;
    }

    public void onLine(String line) {
        if (profile.shouldDrop()) return;
        profile.maybeDelay();

        var m = Message.decode(line);
        switch (m.type()) {
            case PREPARE -> {
                var reply = acceptor.onPrepare(m);
                net.send(m.from(), reply);
            }
            case PROMISE -> {
                // In your PROMISE, proposalNumber = n_a (may be empty), value = v_a (may be empty)
                log.info("PROMISE from " + m.from().value()
                        + " n_a=" + (m.proposalNumber() == null ? "" : m.proposalNumber())
                        + " v_a=" + (m.value() == null ? "" : m.value()));
                // Pass n_a and v_a to the proposer; proposer knows its current round (curN)
                proposer.onPromise(m.proposalNumber(), m.value());
            }
            case ACCEPT_REQUEST -> {
                var reply = acceptor.onAcceptRequest(m);
                net.send(m.from(), reply);
            }
            case ACCEPTED -> {
                var decided = learner.onAccepted(m);
                if (decided != null) {
                    String key = m.proposalNumber(); // or proposalNumber + ":" + decided
                    if (decidedBroadcasted.add(key)) {
                        log.info("CONSENSUS: " + decided + " has been elected Council President!");
                        net.broadcast(new Message(MessageType.DECIDE, me, null, m.proposalNumber(), decided));
                    }
                }
                proposer.onAccepted(m.proposalNumber());
            }

            case DECIDE -> {
                String key = m.proposalNumber() + ":" + m.value();
                if (decideSeen.add(key)) {
                    log.info("DECIDE received: " + m.value());
                }
                // optional: tell local learner so it stops tallying
                // learner.forceDecide(m.proposalNumber(), m.value());
            }

            case NACK -> {
                // In your NACK, proposalNumber carries n_p (the acceptor’s highest promised)
                log.info("NACK from " + m.from().value() + " suggested n_p=" + m.proposalNumber());
                proposer.onNack(m.proposalNumber());
            }
        }
    }
}
