package de.uniks.pioneers.dto;

public record CreateMemberDto(
        boolean ready,
        boolean spectator,
        String password) {
}
