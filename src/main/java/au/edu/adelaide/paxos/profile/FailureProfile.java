package au.edu.adelaide.paxos.profile;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Network profile simulating failures by adding delay and dropping messages.
 */
public final class FailureProfile implements NetworkProfile {

    /**
     * Sleeps for the specified time, ignoring interruptions.
     *
     * @param ms milliseconds to sleep
     */
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            // Intentionally ignored: simulated delay interruptions are non-critical.
        }
    }

    @Override
    public void maybeDelay() {
        sleep(rand(50, 250));
    }

    @Override
    public boolean shouldDrop() {
        return ThreadLocalRandom.current().nextDouble() < 0.35;
    }

    // If you want to simulate crashes later, you can flip this based on counters/timers.
    @Override
    public boolean shouldCrash() {
        return false;
    }

    /**
     * Generates a random integer in the inclusive range [a, b].
     *
     * @param a lower bound
     * @param b upper bound
     * @return random integer between {@code a} and {@code b}
     */
    private static int rand(int a, int b) {
        return a + ThreadLocalRandom.current().nextInt(Math.max(1, b - a + 1));
    }
}
