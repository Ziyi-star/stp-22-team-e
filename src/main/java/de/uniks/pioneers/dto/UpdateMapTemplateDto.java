package de.uniks.pioneers.dto;

import de.uniks.pioneers.model.HarborTemplate;
import de.uniks.pioneers.model.TileTemplate;

import java.util.List;

public record UpdateMapTemplateDto(
        String name,
        String icon,
        String description,
        List<TileTemplate> tiles,
        List<HarborTemplate> harbors
) {
}
