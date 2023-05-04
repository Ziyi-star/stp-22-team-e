package de.uniks.pioneers.model;

public record Game(
        String createdAt,
        String updatedAt,
        String _id,
        String name,
        String owner,
        int members,
        boolean started,
        GameSettings settings
) {
}
