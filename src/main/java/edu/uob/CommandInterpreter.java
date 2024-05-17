package edu.uob;

import java.util.Map;

public class CommandInterpreter {
    StringBuffer result;

    public CommandInterpreter() {
        result = new StringBuffer();
    }

    public String commandInterpret(String command) {
        if (CommandParser.action.equalsIgnoreCase("look")) {
            result.append(GameServer.currentLocation.getInfoByLook());
        }
        else if (CommandParser.action.equalsIgnoreCase("inv") ||
                   CommandParser.action.equalsIgnoreCase("inventory")) {
            result.append(GameServer.players.get(CommandParser.playerName).printInventory());
        }
        else if (CommandParser.action.equalsIgnoreCase("get")) {
            actionGet();
        }
        else if (CommandParser.action.equalsIgnoreCase("drop")) {
            actionDrop();
        }
        else if (CommandParser.action.equalsIgnoreCase("goto")) {
            actionGoto();
        }
        else if (CommandParser.action.equalsIgnoreCase("health")) {
            actionCheckHealth();
        }
        else {
            otherAction(command);
        }

        return result.toString();
    }

    public void refreshLocationList() {
        GameServer.locationList.replace(GameServer.currentLocation.getName(), GameServer.currentLocation);
    }

    public void actionGet() {
        if (CommandParser.artefactCnt == 1) {
            Artefacts artefact = GameServer.currentLocation.artefactsAtHere.get(CommandParser.artefact);
            GameServer.players.get(CommandParser.playerName).inventory.put(artefact.getName(), artefact);
            GameServer.currentLocation.artefactsAtHere.remove(CommandParser.artefact);
            result.append("You picked up a ").append(CommandParser.artefact).append("\n");
            refreshLocationList();
        }
        else {
            result.append("Too many artefacts for get.\n");
        }
    }

    public void actionDrop() {
        if (CommandParser.artefactCnt == 1) {
            Artefacts artefact = GameServer.players.get(CommandParser.playerName).inventory.get(CommandParser.artefact);
            GameServer.currentLocation.artefactsAtHere.put(artefact.getName(), artefact);
            GameServer.players.get(CommandParser.playerName).inventory.remove(CommandParser.artefact);
            result.append("You dropped up a ").append(CommandParser.artefact).append("\n");
            refreshLocationList();
        }
        else {
            result.append("Too many artefacts for drop.\n");
        }
    }

    public void actionGoto() {
        GameServer.players.get(CommandParser.playerName).currentLocation = CommandParser.destination;
        GameServer.currentLocation.playersAtHere.remove(CommandParser.playerName);
        refreshLocationList();
        GameServer.currentLocation = GameServer.locationList.get(CommandParser.destination);
        GameServer.currentLocation.playersAtHere.
                put(CommandParser.playerName, GameServer.players.get(CommandParser.playerName));
        result.append(GameServer.currentLocation.getInfoByLook());
        refreshLocationList();
    }

    public void actionCheckHealth() {
        result.append("Your current health is: ").append(GameServer.players.
                get(CommandParser.playerName).health).append("\n");
    }

    public void otherAction(String command) {
        int validActionCnt = 0;
        GameAction validAction = new GameAction();
        for (GameAction gameAction : GameServer.actions.get(CommandParser.action)) {
            if (!gameAction.entitiesRequired.isEmpty()) {
                int requiredEntitiesExistedCnt = 0;
                for (String entity : gameAction.entitiesRequired) {
                    if (entity.equalsIgnoreCase(GameServer.players.get(CommandParser.playerName).currentLocation) ||
                            GameServer.currentLocation.artefactsAtHere.get(entity) != null ||
                            GameServer.currentLocation.charactersAtHere.get(entity) != null ||
                            GameServer.currentLocation.furnitureAtHere.get(entity) != null ||
                            GameServer.currentLocation.pathToOtherLocations.get(entity) != null ||
                            GameServer.players.get(CommandParser.playerName).inventory.get(entity) != null) {
                        requiredEntitiesExistedCnt++;
                    }
                }
                if (requiredEntitiesExistedCnt == gameAction.entitiesRequiredCnt &&
                        isSubjectInCommand(gameAction, command.toLowerCase()) &&
                            !isWrongSubjectInCommand(gameAction)) {
                    validActionCnt++;
                    validAction = gameAction;
                }
            } else {
                validActionCnt++;
                validAction = gameAction;
            }
        }

        actionExecutor(validActionCnt, validAction);
    }

    public boolean isSubjectInCommand(GameAction gameAction, String command) {
        for (String entity : gameAction.entitiesRequired) {
            if (command.contains(entity)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWrongSubjectInCommand(GameAction gameAction) {
        for (String entity : CommandParser.entitiesInCommand) {
            if (!isSubjectInCommand(gameAction, entity)) {
                return true;
            }
        }
        return false;
    }

    public void actionExecutor(int validActionCnt, GameAction validAction) {
        if (validActionCnt > 1) {
            result.append("There is more than one action possible, which one do you want to perform ?\n");
        }
        else if (validActionCnt == 1) {
            executeAction(validAction);
            result.append(validAction.narration).append("\n");
            if (GameServer.players.get(CommandParser.playerName).health == 0) {
                GameServer.players.get(CommandParser.playerName).resetAfterDeath();
                GameServer.currentLocation.playersAtHere.remove(CommandParser.playerName);
                refreshLocationList();
                GameServer.currentLocation = GameServer.locationList.
                        get(GameServer.players.get(CommandParser.playerName).currentLocation);
                GameServer.currentLocation.playersAtHere.
                        put(CommandParser.playerName, GameServer.players.get(CommandParser.playerName));
                result.append("You died and lost all of your items, you must return to the start of the game.\n");
            }
            refreshLocationList();
        }
        else {
            result.append("Acton failed due to entities required for the action is not all exist\n");
            result.append("in the location and your inventory / command is incomplete.\n");
        }
    }

    public void executeAction(GameAction gameAction) {
        // Consume entities
        if (!gameAction.entitiesConsumed.isEmpty()) {
            for (String entity : gameAction.entitiesConsumed) {
                if (GameServer.currentLocation.artefactsAtHere.get(entity) != null) {
                    GameServer.locationList.get("storeroom").artefactsAtHere.
                            put(entity, GameServer.currentLocation.artefactsAtHere.get(entity));
                    GameServer.currentLocation.artefactsAtHere.remove(entity);
                }
                else if (GameServer.currentLocation.furnitureAtHere.get(entity) != null) {
                    GameServer.locationList.get("storeroom").furnitureAtHere.
                            put(entity, GameServer.currentLocation.furnitureAtHere.get(entity));
                    GameServer.currentLocation.furnitureAtHere.remove(entity);
                }
                else if (GameServer.currentLocation.charactersAtHere.get(entity) != null) {
                    GameServer.locationList.get("storeroom").charactersAtHere.
                            put(entity, GameServer.currentLocation.charactersAtHere.get(entity));
                    GameServer.currentLocation.charactersAtHere.remove(entity);
                }
                else if (GameServer.currentLocation.pathToOtherLocations.get(entity) != null) {
                    GameServer.currentLocation.pathToOtherLocations.remove(entity);
                }
                else if (GameServer.players.get(CommandParser.playerName).inventory.get(entity) != null) {
                    GameServer.locationList.get("storeroom").artefactsAtHere.
                            put(entity, GameServer.players.get(CommandParser.playerName).inventory.get(entity));
                    GameServer.players.get(CommandParser.playerName).inventory.remove(entity);
                }
                else if (entity.equalsIgnoreCase("health")) {
                    if (GameServer.players.get(CommandParser.playerName).health > 0) {
                        GameServer.players.get(CommandParser.playerName).health--;
                    }
                }
            }
        }
        // Produce entities
        entitiesProducer(gameAction);
    }

    public void entitiesProducer(GameAction gameAction) {
        if (!gameAction.entitiesProduced.isEmpty()) {
            for (String entity : gameAction.entitiesProduced) {
                if (GameServer.locationList.get(entity) != null) {
                    GameServer.currentLocation.pathToOtherLocations.
                            put(entity, GameServer.locationList.get(entity));
                }
                else if (entity.equalsIgnoreCase("health")) {
                    if (GameServer.players.get(CommandParser.playerName).health < 3) {
                        GameServer.players.get(CommandParser.playerName).health++;
                    }
                }
                else {
                    checkOtherLocations(entity);
                }
            }
        }
    }

    public void checkOtherLocations(String entity) {
        for (Map.Entry<String, Locations> entry : GameServer.locationList.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(GameServer.currentLocation.getName())) {
                if (entry.getValue().artefactsAtHere.get(entity) != null) {
                    GameServer.currentLocation.artefactsAtHere.
                            put(entity, entry.getValue().artefactsAtHere.get(entity));
                    GameServer.locationList.get(entry.getKey()).artefactsAtHere.remove(entity);
                }
                else if (entry.getValue().charactersAtHere.get(entity) != null) {
                    GameServer.currentLocation.charactersAtHere.
                            put(entity, entry.getValue().charactersAtHere.get(entity));
                    GameServer.locationList.get(entry.getKey()).charactersAtHere.remove(entity);
                }
                else if (entry.getValue().furnitureAtHere.get(entity) != null) {
                    GameServer.currentLocation.furnitureAtHere.
                            put(entity, entry.getValue().furnitureAtHere.get(entity));
                    GameServer.locationList.get(entry.getKey()).furnitureAtHere.remove(entity);
                }
            }
        }
    }
}
