package au.edu.adelaide.paxos.app;

import au.edu.adelaide.paxos.core.MemberId;
import au.edu.adelaide.paxos.profile.*;
import au.edu.adelaide.paxos.roles.impl.DefaultAcceptor;
import au.edu.adelaide.paxos.roles.impl.DefaultLearner;
import au.edu.adelaide.paxos.roles.impl.DefaultProposer;
import au.edu.adelaide.paxos.transport.TransportClient;
import au.edu.adelaide.paxos.transport.TransportServer;
import au.edu.adelaide.paxos.transport.impl.TcpTransportClient;
import au.edu.adelaide.paxos.transport.impl.TcpTransportServer;
import au.edu.adelaide.paxos.util.Config;
import au.edu.adelaide.paxos.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class CouncilMember {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: Mx --profile reliable|standard|latent|failure [--config path]");
            System.exit(1);
        }
        var me = new MemberId(args[0]);
        var profileName = args[1].equals("--profile") ? args[2] : "standard";
        var cfgPath = "src/main/resources/network.config";
        for (int i = 0; i < args.length-1; i++)
            if (args[i].equals("--config")) cfgPath = args[i+1];

        NetworkProfile profile = switch (profileName.toLowerCase()) {
            case "reliable" -> new ReliableProfile();
            case "latent"   -> new LatentProfile();
            case "failure"  -> new FailureProfile();
            default         -> new StandardProfile();
        };

        var cfg = new Config(cfgPath);
        var log = new Log(me.value());
        var serverEntry = cfg.byId().get(me.value());
        if (serverEntry == null) throw new IllegalStateException("Unknown member " + me.value());

        TransportServer server = new TcpTransportServer(serverEntry.port());
        TransportClient client = new TcpTransportClient(cfg);

        var acceptor = new DefaultAcceptor();
        var learner  = new DefaultLearner(cfg.byId().size());
        var proposer = new DefaultProposer(me, cfg.byId().size(), client);
        var router   = new MessageRouter(me, profile, acceptor, learner, proposer, client, log);

        server.start(router::onLine);
        log.info("Started on port " + serverEntry.port() + " with profile " + profileName);

        // stdin = propose a candidate (e.g., "M5")
        try (var br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                var v = line.trim(); if (v.isEmpty()) continue;
                log.info("Proposing candidate: " + v);
                proposer.propose(v);
            }
        }
    }
}
