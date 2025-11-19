package au.edu.adelaide.paxos.profile;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Default network profile simulating modest delay and a small drop probability.
 */
public final class StandardProfile implements NetworkProfile {

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
        sleep(rand(10, 150));
    }

    @Override
    public boolean shouldDrop() {
        return ThreadLocalRandom.current().nextDouble() < 0.02;
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
