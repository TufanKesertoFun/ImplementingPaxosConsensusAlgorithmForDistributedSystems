package au.edu.adelaide.paxos.roles.impl;

import au.edu.adelaide.paxos.core.*;
import au.edu.adelaide.paxos.roles.Acceptor;

public final class DefaultAcceptor implements Acceptor {

    private ProposalNumber n_p; // highest promised
    private ProposalNumber n_a; // last accepted n
    private Value v_a;          // last accepted v

    @Override
    public synchronized Message onPrepare(Message m) {
        var n = ProposalNumber.parse(m.proposalNumber());

        if (n_p == null || n.compareTo(n_p) >= 0) {
            // Promise for this round n; return previously accepted (n_a, v_a) if any
            n_p = n;
            String naStr = (n_a == null) ? "" : n_a.toString();
            String vaStr = (v_a == null) ? "" : v_a.candidate();

            // PROMISE must carry the PREPARE round n in proposalNumber,
            // and na/va as a readable payload.
            return new Message(
                    MessageType.PROMISE, m.to(), m.from(),
                    n.toString(),
                    "na=" + naStr + ";va=" + vaStr
            );
        }

        // Reject with highest promised as np (in payload)
        return new Message(
                MessageType.NACK, m.to(), m.from(),
                "", "np=" + n_p.toString()
        );
    }

    @Override
    public synchronized Message onAcceptRequest(Message m) {
        var n = ProposalNumber.parse(m.proposalNumber());

        // Extract candidate from payload (support both "v=M5" and legacy raw "M5")
        String payload = (m.value() == null) ? "" : m.value().trim();
        String candidate = extractV(payload);

        if (n_p == null || n.compareTo(n_p) >= 0) {
            n_p = n;
            n_a = n;
            v_a = new Value(candidate);

            // Return ACCEPTED with n and v=...
            return new Message(
                    MessageType.ACCEPTED, m.to(), m.from(),
                    n.toString(),
                    "v=" + candidate
            );
        }

        // Reject with highest promised as np (in payload)
        return new Message(
                MessageType.NACK, m.to(), m.from(),
                "", "np=" + n_p.toString()
        );
    }

    // ---- helpers ----
    private static String extractV(String payload) {
        if (payload == null || payload.isBlank()) return "";
        int idx = payload.indexOf("v=");
        if (idx >= 0) {
            return payload.substring(idx + 2).trim();
        }
        // legacy: whole value is the candidate
        return payload.trim();
    }
}
