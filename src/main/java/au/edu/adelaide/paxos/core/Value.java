package au.edu.adelaide.paxos.core;

/**
 * Simple wrapper representing a candidate value chosen by Paxos.
 *
 * @param candidate identifier of the chosen candidate (e.g. "M5")
 */
public record Value(String candidate) {
}
