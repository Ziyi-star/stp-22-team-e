package de.uniks.pioneers.model;

public record Tile(
        Number x,
        Number y,
        Number z,
        String type,
        int numberToken
) {
}
