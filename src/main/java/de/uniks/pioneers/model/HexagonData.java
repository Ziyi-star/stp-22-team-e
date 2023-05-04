package de.uniks.pioneers.model;

/*
    Class which implements SatelliteData, allowing us to store arbitrary data within defined Hexagons.
 */


import org.hexworks.mixite.core.api.Point;
import org.hexworks.mixite.core.api.contract.SatelliteData;

import java.util.Collection;
import java.util.HashMap;

public class HexagonData implements SatelliteData {

    private final HashMap<Point, Integer> hexPointToClock = new HashMap<>();           //Translation Map to tell where the point is relative to the Hexagon clockwise
    private final int id;                                           //unique identifier for this hexagon
    private final Tile tile;                                       //The associated Tile from the map data which contains more information
    private final Collection<HexagonPoint> hexagonPoints;          //The HexagonPoints which touch this Hexagon.
    private boolean isPassable;
    private boolean isOpaque;
    private double movementCost;

    public HexagonData(boolean isPassable, boolean isOpaque, double movementCost, Tile tile, Collection<HexagonPoint> hexagonPoints, int id) {
        this.isPassable = isPassable;
        this.isOpaque = isOpaque;
        this.movementCost = movementCost;
        this.tile = tile;
        this.hexagonPoints = hexagonPoints;
        this.id = id;
    }

    @Override
    public boolean getPassable() {
        return isPassable;
    }

    @Override
    public void setPassable(boolean b) {
        isPassable = b;
    }

    @Override
    public boolean getOpaque() {
        return isOpaque;
    }

    @Override
    public void setOpaque(boolean b) {
        isOpaque = b;
    }

    @Override
    public double getMovementCost() {
        return movementCost;
    }

    @Override
    public void setMovementCost(double v) {
        movementCost = v;
    }

    public Tile getTile() {
        return tile;
    }

    public Collection<HexagonPoint> getHexagonPoints() {
        return hexagonPoints;
    }

    public HashMap<Point, Integer> getHexPointToClock() {
        return hexPointToClock;
    }

    public int getId() {
        return id;
    }
}
