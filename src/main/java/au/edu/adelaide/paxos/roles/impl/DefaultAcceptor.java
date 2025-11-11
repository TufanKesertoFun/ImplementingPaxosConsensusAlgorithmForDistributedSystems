package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.*;
import au.edu.adelaide.paxos.roles.Acceptor;

public final class DefaultAcceptor implements Acceptor {

    private ProposalNumber n_p;
    private ProposalNumber n_a;
    private Value v_a;

    @Override
    public synchronized Message onPrepare(Message m) {

        var n = ProposalNumber.parse(m.proposalNumber());

        if (n_p == null || n.compareTo(n_p) >= 0){
            n_p = n;
            return new Message(
                    MessageType.PROMISE, m.to(), m.from(),
                    n_a == null ? "" : n_a.toString(),
                    v_a == null ? "" : v_a.candidate()
            );
        }
        return new Message(MessageType.NACK, m.to(), m.from(), n_p.toString(), null);

    }

    @Override public synchronized Message onAcceptRequest(Message m){

        var n = ProposalNumber.parse(m.proposalNumber());

        if (n_p == null || n.compareTo(n_p) >= 0){
            n_p = n; n_a = n; v_a = new Value(m.value());

            return new Message(MessageType.ACCEPTED, m.to(), m.from(), n.toString(), v_a.candidate());
        }
        return new Message(MessageType.NACK, m.to(), m.from(), n_p.toString(), null);
    }
}
