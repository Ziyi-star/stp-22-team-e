package de.uniks.pioneers.model;

import java.util.List;

public record Map(
        String gameId,
        List<Tile> tiles,
        List<Harbor> harbors
) {
}
