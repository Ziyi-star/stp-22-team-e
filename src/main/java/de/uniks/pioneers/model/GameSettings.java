package de.uniks.pioneers.model;

public record GameSettings(
        int mapRadius,
        int victoryPoints,
        String mapTemplate,
        boolean roll7,
        Integer startingResources
) {
    public GameSettings(int mapRadius, int victoryPoints) {
        this(mapRadius, victoryPoints,null, false, 10);
    }

    public GameSettings(int mapRadius, int victoryPoints, String mapId) {
        this(mapRadius, victoryPoints,mapId, true, null);
    }
}
