package au.edu.adelaide.paxos.util;

import java.nio.file.*; import java.util.*;

public final class Config {
    public record Entry(String id, String host, int port) {}
    private final Map<String, Entry> byId = new LinkedHashMap<>();
    public Config(String path) {
        try {
            for (String l : Files.readAllLines(Path.of(path))) {
                l = l.trim(); if (l.isEmpty() || l.startsWith("#")) continue;
                var p = l.split(",");
                byId.put(p[0], new Entry(p[0], p[1], Integer.parseInt(p[2])));
            }
        } catch (Exception e) { throw new RuntimeException("config read fail", e); }
    }
    public Map<String, Entry> byId(){ return byId; }
}
