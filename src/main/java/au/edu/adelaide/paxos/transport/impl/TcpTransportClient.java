package au.edu.adelaide.paxos.transport.impl;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.core.Message;
import au.edu.adelaide.paxos.transport.TransportClient;
import au.edu.adelaide.paxos.util.Config;

import java.io.*;
import java.net.Socket;

public final class TcpTransportClient implements TransportClient {
    private final Config cfg;

    public TcpTransportClient(Config cfg) { this.cfg = cfg; }

    @Override public void send(MemberId to, Message m) {
        var e = cfg.byId().get(to.value());
        if (e == null) return;
        try (Socket s = new Socket(e.host(), e.port());
             var w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            w.write(m.encode()); w.flush();
        } catch (IOException ignored) { }
    }

    @Override public void broadcast(Message m) {
        for (var id : cfg.byId().keySet()) {
            if (!id.equals(m.from().value())) send(new MemberId(id), new Message(
                    m.type(), m.from(), new MemberId(id), m.proposalNumber(), m.value()
            ));
        }
    }
}
