package au.edu.adelaide.paxos.profile;

import java.util.concurrent.ThreadLocalRandom;

public final class LatentProfile implements NetworkProfile {
    private static void sleep(long ms){ try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    @Override public void maybeDelay() {
        // High jitter 300–1500ms, plus a rare burst
        sleep(rand(300, 1500));
        if (ThreadLocalRandom.current().nextDouble() < 0.10) sleep(rand(500, 2000));
    }
    @Override public boolean shouldDrop() { return ThreadLocalRandom.current().nextDouble() < 0.03; }
    private static int rand(int a, int b){ return a + ThreadLocalRandom.current().nextInt(Math.max(1, b - a + 1)); }
}
