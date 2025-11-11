package au.edu.adelaide.paxos.profile;

import java.util.concurrent.ThreadLocalRandom;

public final class StandardProfile implements NetworkProfile {
    private static void sleep(long ms){ try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    @Override public void maybeDelay() { sleep(rand(10, 150)); }
    @Override public boolean shouldDrop() { return ThreadLocalRandom.current().nextDouble() < 0.02; }
    private static int rand(int a, int b){ return a + ThreadLocalRandom.current().nextInt(Math.max(1, b - a + 1)); }
}
