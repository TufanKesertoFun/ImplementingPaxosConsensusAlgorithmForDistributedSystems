package au.edu.adelaide.paxos.profile;

public interface NetworkProfile {
    void maybeDelay();
    boolean shouldDrop();
    default boolean shouldCrash() { return false; }
}
