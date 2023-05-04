package de.uniks.pioneers.dto;

public record CreateMoveDto(
        String action,
        RobDto rob,
        ResourcesDto resources,
        String partner,
        String developmentCard,
        CreateBuildingDto building
) {
    public CreateMoveDto(String action, RobDto rob, ResourcesDto resources, String partner, CreateBuildingDto building) {
        this(action, rob, resources, partner, null, building);
    }
}
