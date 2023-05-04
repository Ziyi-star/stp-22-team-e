package de.uniks.pioneers.model;

import org.hexworks.mixite.core.api.CubeCoordinate;

public record MapToGridInfo(
        int grid_height,
        int grid_width,
        CubeCoordinate centerCoord
) {
}
