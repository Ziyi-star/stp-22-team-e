package de.uniks.pioneers.dto;

import java.util.List;

public record CreateGroupDto(
        String name,
        List<String> members
) {
}
