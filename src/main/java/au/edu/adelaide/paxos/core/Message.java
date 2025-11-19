package au.edu.adelaide.paxos.core;

/**
 * Immutable representation of a Paxos protocol message using a simple text format.
 *
 * @param type           message type enum
 * @param from           sender member id
 * @param to             receiver member id (nullable for broadcast)
 * @param proposalNumber proposal number string (nullable for some types)
 * @param value          payload string (e.g. key=value pairs)
 */
public record Message(
        MessageType type,
        MemberId from,
        MemberId to,
        String proposalNumber,
        String value
) {

    /**
     * Encodes this message into the wire text format: fields are separated by {@code |}
     * and terminated with {@code \n}.
     *
     * @return encoded message line ready to send over a socket
     */
    public String encode() {
        return String.join("|",
                type.name(),
                from.toString(),
                to == null ? "" : to.toString(),
                proposalNumber == null ? "" : proposalNumber,
                value == null ? "" : value
        ) + "\n";
    }

    /**
     * Decodes a serialized message line into a {@link Message} record.
     *
     * @param line raw line containing a full message
     * @return decoded {@link Message}
     */
    public static Message decode(String line) {
        var p = line.trim().split("\\|", -1);
        return new Message(
                MessageType.valueOf(p[0]),
                new MemberId(p[1]),
                p[2].isEmpty() ? null : new MemberId(p[2]),
                p[3].isEmpty() ? null : p[3],
                p[4].isEmpty() ? null : p[4]
        );
    }
}
