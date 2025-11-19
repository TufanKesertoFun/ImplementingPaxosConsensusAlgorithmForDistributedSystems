package au.edu.adelaide.paxos.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Simple time-stamped console logger used for human-readable traces.
 */
public final class Log {

    private final String id;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * Creates a logger tagged with a given id (e.g. member id).
     *
     * @param id identifier printed with each log line
     */
    public Log(String id) {
        this.id = id;
    }

    /**
     * Logs an informational message with time and id prefix.
     *
     * @param msg message to print
     */
    public void info(String msg) {
        System.out.println("[" + F.format(LocalTime.now()) + "][" + id + "] " + msg);
    }
}
