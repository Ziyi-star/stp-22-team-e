package de.uniks.pioneers.model;

import java.util.List;

public record State(
        String updatedAt,
        String gameId,
        List<ExpectedMove> expectedMoves,
        Point3D robber,

        String winner
) {

    public State(String updatedAt, String gameId, List<ExpectedMove> expectedMoves, Point3D robber) {
        this(updatedAt, gameId,expectedMoves, robber, null);
    }
}
