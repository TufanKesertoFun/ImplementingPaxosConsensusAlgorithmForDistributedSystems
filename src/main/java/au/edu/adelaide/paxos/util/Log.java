package au.edu.adelaide.paxos.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class Log {
    private final String id;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public Log(String id) { this.id = id; }
    public void info(String msg) { System.out.println("[" + F.format(LocalTime.now()) + "][" + id + "] " + msg); }
}
