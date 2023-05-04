package de.uniks.pioneers.model;

public record Building(
        Number x,
        Number y,
        Number z,
        String _id,
        Number side,
        String type,
        String gameId,
        String owner
) {
}
