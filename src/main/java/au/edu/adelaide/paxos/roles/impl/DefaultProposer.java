package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;
import au.edu.adelaide.paxos.core.MessageType;
import au.edu.adelaide.paxos.core.ProposalNumber;
import au.edu.adelaide.paxos.roles.Proposer;
import au.edu.adelaide.paxos.transport.TransportClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultProposer implements Proposer {

    private final MemberId me;
    private final int members;
    private final TransportClient net;
    private int localCounter = 0;

    private final Map<String,Integer> promiseCount = new ConcurrentHashMap<>();
    private final Map<String,Integer> acceptedCount = new ConcurrentHashMap<>();

    public DefaultProposer(MemberId me, int members, TransportClient net){
        this.me = me;
        this.members = members;
        this.net = net;
    }

    private ProposalNumber nextN() {
        localCounter++;
        int proposerId = Integer.parseInt(me.value().substring(1));
        return new ProposalNumber(localCounter, proposerId);
    }

    @Override
    public void propose(String candidate){
        var n = nextN();

        net.broadcast(new Message(MessageType.PREPARE, me, null, n.toString(), null));
    }

    public void onPromise(String n) {

        promiseCount.merge(n, 1, Integer::sum);
        if (promiseCount.get(n) >= quorum()){

        }
    }

    public void onAccepted(String n) {
        acceptedCount.merge(n, 1, Integer::sum);
        if (acceptedCount.get(n) >= quorum()){

        }
    }

    private int quorum() {
        return members/2 + 1;
    }
}
