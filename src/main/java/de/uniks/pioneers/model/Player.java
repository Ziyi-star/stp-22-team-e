package de.uniks.pioneers.model;

import java.util.List;

public record Player(
        String gameId,
        String userId,
        String color,
        boolean active,
        int foundingRoll,
        Resources resources,
        RemainingBuildings remainingBuildings,
        Number victoryPoints,
        Number longestRoad,
        boolean hasLongestRoad,
        boolean hasLargestArmy,
        PreviousTradeOffer previousTradeOffer,
        List<DevelopmentCard> developmentCards
) {
    public Player(String gameId, String userId, String color, boolean active, int foundingRoll, Resources resources, RemainingBuildings remainingBuildings, Number victoryPoints, Number longestRoad, PreviousTradeOffer previousTradeOffer) {
        this(gameId, userId, color, active, foundingRoll, resources, remainingBuildings, victoryPoints, longestRoad, false, false, previousTradeOffer, null);
    }
}
