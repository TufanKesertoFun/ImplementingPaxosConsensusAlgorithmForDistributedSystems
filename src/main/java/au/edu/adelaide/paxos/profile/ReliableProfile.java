package au.edu.adelaide.paxos.profile;

/**
 * Network profile with no artificial delay and no packet drops.
 */
public final class ReliableProfile implements NetworkProfile {

    @Override
    public void maybeDelay() {
        /* no delay */
    }

    @Override
    public boolean shouldDrop() {
        return false;
    }
}
