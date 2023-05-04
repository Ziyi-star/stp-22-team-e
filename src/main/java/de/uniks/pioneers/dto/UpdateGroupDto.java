package de.uniks.pioneers.dto;

import java.util.List;

public record UpdateGroupDto(
        String name,
        List<String> members
) {
}
