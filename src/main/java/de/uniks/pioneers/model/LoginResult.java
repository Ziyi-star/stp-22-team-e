package de.uniks.pioneers.model;

import java.util.List;

public record LoginResult(
        String createdAt,
        String updatedAt,
        String _id,
        String name,
        String status,
        String avatar,
        List<String> friends,
        String accessToken,
        String refreshToken) {
}
