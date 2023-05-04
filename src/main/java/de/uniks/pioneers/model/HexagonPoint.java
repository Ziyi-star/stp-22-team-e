package de.uniks.pioneers.model;

import org.hexworks.mixite.core.api.Hexagon;
import org.hexworks.mixite.core.api.Point;

import java.util.Collection;

public record HexagonPoint(

        Point point,                                            //Pixel-Coordinate of the point

        Collection<HexagonPoint> pointNeighbors,                //Neighbor HexagonPoints

        Collection<Hexagon<HexagonData>> hexagonNeighbors,      //Neighbor Hexagons (the ones touching the point)

        Hexagon<HexagonData> hexagonOwner,
        //Which Hexagon is my "owner" - null means it's owned by the ocean (Border)

        Collection<HexagonPoint> pointRoadNeighbors,             //Neighbor Road HexagonPoints

        boolean isRoadHexagonPoint                               // flag if this hexagonPoint is a Road Point

) {
}
