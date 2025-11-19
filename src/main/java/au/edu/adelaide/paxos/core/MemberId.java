package au.edu.adelaide.paxos.core;

/**
 * Represents a unique member ID (e.g., "M1", "M2", …).
 * Immutable value object; equality and hash code are based on the string value.
 */
public final class MemberId {

    private final String value;

    /**
     * Creates a new member identifier.
     *
     * @param value textual member id, e.g. "M1"
     */
    public MemberId(String value) {
        this.value = value;
    }

    /**
     * Returns the raw string value of this member id.
     *
     * @return member id as string
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberId other)) {
            return false;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
