package de.uniks.pioneers.model;

import java.util.List;

public record MapTemplate(
        String createdAt,
        String updatedAt,
        String _id,
        String name,
        String icon,
        String description,
        String createdBy,
        Number votes,
        List<TileTemplate> tiles,
        List<HarborTemplate> harbors
) {

    public MapTemplate(String createdAt, String updatedAt, String _id, String name, String icon, String createdBy, Number votes, List<TileTemplate> tiles, List<HarborTemplate> harbors) {
        this(createdAt,updatedAt,_id,name,icon,null,createdBy,votes,tiles,harbors);
    }
}
