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

/**
 * Entry point for a single council member process participating in Paxos.
 * <p>
 * Usage:
 * {@code Mx --profile reliable|standard|latent|failure [--config path] [--propose <candidate>]}
 */
public final class CouncilMember {

    /**
     * Starts a council member with the given arguments and joins the Paxos protocol.
     *
     * @param args command line arguments:
     *             {@code args[0]} = member id (e.g. "M1"),
     *             {@code --profile} (optional),
     *             {@code --config} (optional path to network.config),
     *             {@code --propose} (optional auto-proposal candidate)
     * @throws Exception if configuration or networking fails during startup
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: Mx --profile reliable|standard|latent|failure [--config path] [--propose <candidate>]");
            System.exit(1);
        }

        var me = new MemberId(args[0]);
        var profileName = args[1].equals("--profile") ? args[2] : "standard";
        var cfgPath = "src/main/resources/network.config";

        String autoPropose = null; // <-- add

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--config")) {
                cfgPath = args[i + 1];
            }
            if (args[i].equals("--propose")) {
                autoPropose = args[i + 1]; // <-- add
            }
        }

        NetworkProfile profile = switch (profileName.toLowerCase()) {
            case "reliable" -> new ReliableProfile();
            case "latent" -> new LatentProfile();
            case "failure" -> new FailureProfile();
            default -> new StandardProfile();
        };

        var cfg = new Config(cfgPath);
        var log = new Log(me.value());
        var serverEntry = cfg.byId().get(me.value());
        if (serverEntry == null) {
            throw new IllegalStateException("Unknown member " + me.value());
        }

        TransportServer server = new TcpTransportServer(serverEntry.port());
        TransportClient client = new TcpTransportClient(cfg);

        var acceptor = new DefaultAcceptor();
        var learner = new DefaultLearner(cfg.byId().size());
        var proposer = new DefaultProposer(me, cfg.byId().size(), client);
        var router = new MessageRouter(me, profile, acceptor, learner, proposer, client, log);

        server.start(router::onLine);
        log.info("Started on port " + serverEntry.port() + " with profile " + profileName);

        // <-- NEW: auto-propose if requested
        // <-- NEW: auto-propose if requested (with small startup delay)
        if (autoPropose != null && !autoPropose.isBlank()) {
            String candidate = autoPropose;
            new Thread(() -> {
                try {
                    // Give other members time to start their servers
                    Thread.sleep(2000); // 2 seconds; you can increase to 3000 if needed
                } catch (InterruptedException ignored) {
                    // Intentionally ignored: auto-propose thread interruption is non-critical.
                }

                log.info("Auto-proposing candidate: " + candidate);
                proposer.propose(candidate);
            }, "AutoPropose-" + me.value()).start();
        }

        // stdin = propose a candidate interactively (still supported)
        try (var br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                var v = line.trim();
                if (v.isEmpty()) {
                    continue;
                }
                log.info("Proposing candidate: " + v);
                proposer.propose(v);
            }
        }
    }
}
