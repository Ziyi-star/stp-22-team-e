package de.uniks.pioneers.dto;

import de.uniks.pioneers.model.HarborTemplate;
import de.uniks.pioneers.model.TileTemplate;

import java.util.List;

public record CreateMapTemplateDto(
        String name,
        String icon,
        String description,
        List<TileTemplate> tiles,
        List<HarborTemplate> harbors
) {
}
