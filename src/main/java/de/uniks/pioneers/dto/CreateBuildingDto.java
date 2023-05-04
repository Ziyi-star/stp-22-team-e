package de.uniks.pioneers.dto;

public record CreateBuildingDto(
        Number x,
        Number y,
        Number z,
        Number side,
        String type
) {
}
