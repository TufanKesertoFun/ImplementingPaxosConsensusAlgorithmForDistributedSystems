package au.edu.adelaide.paxos.core;

public record Message(
        MessageType type,
        MemberId from,
        MemberId to,
        String proposalNumber,
        String value
){
    public String encode() {
        return String.join("|",
                type.name(),
                from.toString(),
                to == null ? "" : to.toString(),
                proposalNumber == null ? "" : proposalNumber,
                value == null ? "" : value
        ) + "\n";
    }

    public static Message decode(String line){
        var p = line.trim().split("\\|", -1);
        return new Message(
                MessageType.valueOf(p[0]),
                new MemberId(p[1]),
                p[2].isEmpty() ? null : new MemberId(p[2]),
                p[3].isEmpty() ? null : p[3],
                p[4].isEmpty() ? null : p[4]);
    }
}
