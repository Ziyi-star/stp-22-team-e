package de.uniks.pioneers.model;

import java.util.List;

public record User(
        String _id,
        String name,
        String status,
        String avatar,
        List<String> friends) {
}
