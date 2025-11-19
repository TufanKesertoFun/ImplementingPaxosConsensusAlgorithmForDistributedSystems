package au.edu.adelaide.paxos.profile;

/**
 * Abstraction for simulating different network conditions:
 * delay, packet drop and (optionally) crashes.
 */
public interface NetworkProfile {

    /**
     * Optionally sleeps to simulate network latency.
     */
    void maybeDelay();

    /**
     * Indicates whether the current message should be dropped.
     *
     * @return {@code true} if the message should be dropped
     */
    boolean shouldDrop();

    /**
     * Indicates whether the process should crash under this profile.
     * Default is never crash.
     *
     * @return {@code true} if a crash should be simulated
     */
    default boolean shouldCrash() {
        return false;
    }
}
