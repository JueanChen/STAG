package edu.uob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Players extends GameEntity{
    int health;
    String startLocation;
    String currentLocation;
    HashMap<String, Artefacts> inventory;
    public Players(String name, String description) {
        super(name, description);
        health = 3;
        inventory = new HashMap<>();
        startLocation = "";
        currentLocation = "";
    }

    public String printInventory() {
        StringBuffer invInfo = new StringBuffer();
        invInfo.append("You have:\n");
        if (!inventory.isEmpty()) {
            for (Map.Entry<String, Artefacts> entry : inventory.entrySet()) {
                invInfo.append(entry.getValue().getDescription()).append("\n");
            }
        }
        return invInfo.toString();
    }

    public void resetAfterDeath() {
        GameServer.currentLocation.artefactsAtHere.putAll(inventory);
        inventory.clear();
        health = 3;
        currentLocation = startLocation;
    }
}
