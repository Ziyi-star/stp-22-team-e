package de.uniks.pioneers.dto;


public record CreateUserDto(
        String name,
        String avatar,
        String password) {
}
