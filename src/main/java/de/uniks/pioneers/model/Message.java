package de.uniks.pioneers.model;

public record Message(
        String createdAt,
        String updatedAt,
        String _id,
        String sender,
        String body
) {
}
