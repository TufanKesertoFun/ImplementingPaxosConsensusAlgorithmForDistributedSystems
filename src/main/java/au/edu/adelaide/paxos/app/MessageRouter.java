package au.edu.adelaide.paxos.app;

import au.edu.adelaide.paxos.core.*;
import au.edu.adelaide.paxos.profile.NetworkProfile;
import au.edu.adelaide.paxos.roles.*;
import au.edu.adelaide.paxos.roles.impl.DefaultProposer;
import au.edu.adelaide.paxos.transport.TransportClient;
import au.edu.adelaide.paxos.util.Log;

public final class MessageRouter {
    private final MemberId me;
    private final NetworkProfile profile;
    private final Acceptor acceptor;
    private final Learner learner;
    private final DefaultProposer proposer; // so we can notify on PROMISE/ACCEPTED
    private final TransportClient net;
    private final Log log;

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
                log.info("PROMISE from " + m.from().value() + " for n=" + m.proposalNumber());
                proposer.onPromise(m.proposalNumber());
            }
            case ACCEPT_REQUEST -> {
                var reply = acceptor.onAcceptRequest(m);
                net.send(m.from(), reply);
            }
            case ACCEPTED -> {
                var decided = learner.onAccepted(m);
                if (decided != null) {
                    log.info("CONSENSUS: " + decided + " has been elected Council President!");
                    // broadcast DECIDE for completeness
                    net.broadcast(new Message(MessageType.DECIDE, me, null, m.proposalNumber(), decided));
                }
                proposer.onAccepted(m.proposalNumber());
            }
            case DECIDE -> log.info("DECIDE received: " + m.value());
            case NACK -> log.info("NACK from " + m.from().value() + " suggested n_p=" + m.proposalNumber());
        }
    }
}
