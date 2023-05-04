package de.uniks.pioneers.dto;

public record UpdateMemberDto(
        boolean ready,
        String color,
        boolean spectator) {
}
