package au.edu.adelaide.paxos.profile;

public final class ReliableProfile implements NetworkProfile {
    @Override public void maybeDelay() { /* no delay */ }
    @Override public boolean shouldDrop() { return false; }
}
