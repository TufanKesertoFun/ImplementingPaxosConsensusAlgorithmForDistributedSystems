package au.edu.adelaide.paxos.profile;

import java.util.concurrent.ThreadLocalRandom;

public final class FailureProfile implements NetworkProfile {
    private static void sleep(long ms){ try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    @Override public void maybeDelay() { sleep(rand(50, 250)); }
    @Override public boolean shouldDrop() { return ThreadLocalRandom.current().nextDouble() < 0.35; }
    // If you want to simulate crashes later, you can flip this based on counters/timers.
    @Override public boolean shouldCrash() { return false; }
    private static int rand(int a, int b){ return a + ThreadLocalRandom.current().nextInt(Math.max(1, b - a + 1)); }
}
