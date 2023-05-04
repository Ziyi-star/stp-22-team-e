package de.uniks.pioneers.dto;

public record RefreshResult(
        String _id,
        String name,
        String status,
        String avatar,
        String accessToken,
        String refreshToken
) {
}
