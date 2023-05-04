package de.uniks.pioneers.service;

import de.uniks.pioneers.model.Game;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameStorage {

    private Game game;

    @Inject
    public GameStorage() {

    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
