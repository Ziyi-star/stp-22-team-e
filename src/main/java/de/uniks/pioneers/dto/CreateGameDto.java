package de.uniks.pioneers.dto;

import de.uniks.pioneers.model.GameSettings;

public record CreateGameDto(
        String name,
        boolean started,
        GameSettings settings,
        String password) {
}
