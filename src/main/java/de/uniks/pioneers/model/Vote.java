package de.uniks.pioneers.model;

public record Vote(
        String createdAt,
        String updatedAt,
        String mapId,
        String userId,
        Number score
) {
}
