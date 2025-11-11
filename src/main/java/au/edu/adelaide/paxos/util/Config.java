// au/edu/adelaide/paxos/util/Config.java
package au.edu.adelaide.paxos.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;

public final class Config {
    public record Entry(String id, String host, int port) {}
    private final Map<String, Entry> byId = new LinkedHashMap<>();

    public Config(String pathOrResource) {
        try {
            List<String> lines = null;

            // 1) Try classpath resource (works when file is in src/main/resources)
            var cl = Thread.currentThread().getContextClassLoader();
            var in = cl.getResourceAsStream(pathOrResource);
            if (in == null) in = cl.getResourceAsStream("network.config");
            if (in != null) {
                try (var br = new BufferedReader(new InputStreamReader(in))) {
                    lines = br.lines().toList();
                }
            }

            // 2) Fallback to filesystem path
            if (lines == null) lines = Files.readAllLines(Path.of(pathOrResource));

            for (String l : lines) {
                l = l.trim();
                if (l.isEmpty() || l.startsWith("#")) continue;
                var p = l.split(",");
                byId.put(p[0], new Entry(p[0], p[1], Integer.parseInt(p[2])));
            }
        } catch (Exception e) {
            throw new RuntimeException("config read fail", e);
        }
    }
    public Map<String, Entry> byId(){ return byId; }
}
