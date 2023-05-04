package de.uniks.pioneers.model;

import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.dto.RobDto;

public record Move(
        String createdAt,
        String _id,
        String gameId,
        String userId,
        String action,
        int roll,
        String building,
        RobDto rob,
        ResourcesDto resources,
        String partner,
        String developmentCard
) {
    public Move(String createdAt, String _id, String gameId, String userId, String action, int roll, String building, RobDto rob, ResourcesDto resources, String partner) {
        this(createdAt, _id, gameId, userId, action, roll, building, rob, resources, partner, null);
    }
}
