package edu.uob;

import java.util.*;

public class Locations extends GameEntity{
    public HashMap<String, Locations> pathToOtherLocations;
    public HashMap<String, Artefacts> artefactsAtHere;
    public HashMap<String, Characters> charactersAtHere;
    public HashMap<String, Furniture> furnitureAtHere;
    public HashMap<String, Players> playersAtHere;

    public Locations(String name, String description) {
        super(name, description);
        this.pathToOtherLocations = new HashMap<>();
        this.artefactsAtHere = new HashMap<>();
        this.charactersAtHere = new HashMap<>();
        this.furnitureAtHere = new HashMap<>();
        this.playersAtHere = new HashMap<>();
    }

    public String getInfoByLook() {
        StringBuffer info = new StringBuffer();

        info.append("You are in ").append(getDescription()).append(". You can see:\n");
        if (!artefactsAtHere.isEmpty()) {
            for (Map.Entry<String, Artefacts> entry : artefactsAtHere.entrySet()) {
                info.append(entry.getValue().getDescription()).append("\n");
            }
        }
        if (!charactersAtHere.isEmpty()) {
            for (Map.Entry<String, Characters> entry : charactersAtHere.entrySet()) {
                info.append(entry.getValue().getDescription()).append("\n");
            }
        }
        if (!furnitureAtHere.isEmpty()) {
            for (Map.Entry<String, Furniture> entry : furnitureAtHere.entrySet()) {
                info.append(entry.getValue().getDescription()).append("\n");
            }
        }
        if (!playersAtHere.isEmpty()) {
            for (Map.Entry<String, Players> entry : playersAtHere.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase(CommandParser.playerName)) {
                    info.append(entry.getKey()).append("\n");
                }
            }
        }
        info.append("You can access from here:\n");
        if (!pathToOtherLocations.isEmpty()) {
            for (Map.Entry<String, Locations> entry : pathToOtherLocations.entrySet()) {
                info.append(entry.getKey()).append("\n");
            }
        }

        return info.toString();
    }
}
