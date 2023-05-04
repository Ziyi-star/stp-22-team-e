package de.uniks.pioneers.service;

import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.RemainingBuildings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class VictoryStorage {

    private final RemainingBuildings startBuildings = new RemainingBuildings(5, 4, 15);

    private Map<String, Integer> army;

    @Inject
    public VictoryStorage() {

    }

    public RemainingBuildings getStartBuildings() {
        return startBuildings;
    }

    public void setKnights(Map<String, Integer> knights) {
        this.army = knights;
    }

    public Map<String, Integer> getKnights() {
        return army;
    }
}
