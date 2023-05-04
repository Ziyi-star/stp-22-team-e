package de.uniks.pioneers.dto;

import java.util.List;

public record UpdateUserDto(
        String name,
        String status,
        String avatar,
        List<String> friends,
        String password
) {
}
