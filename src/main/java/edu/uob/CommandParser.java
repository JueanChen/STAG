package edu.uob;

import java.util.HashSet;
import java.util.Map;

public class CommandParser {
    HashSet<String> basicAction = new HashSet<>() {{
        add("inventory");
        add("inv");
        add("get");
        add("drop");
        add("goto");
        add("look");
        add("health");
    }};
    public static HashSet<String> entitiesInCommand;
    StringBuffer error;
    public static String playerName, action, artefact, character, furniture, destination;
    public static int actionCnt = 0;
    public static int artefactCnt = 0;
    public static int characterCnt = 0;
    public static int furnitureCnt = 0;
    public static int destinationCnt = 0;
    public CommandParser() {
        entitiesInCommand = new HashSet<>();
        error = new StringBuffer();
        actionCnt = 0;
        artefactCnt = 0;
        characterCnt = 0;
        furnitureCnt = 0;
        destinationCnt = 0;
    }

    public boolean checkCommand(String command) {
        command = commandExtractor(command);
        if (playerName != null) {
            if (playerName.isEmpty()) {
                error.append("Player's name is missing, please check.\n");
                return false;
            }
        } else {
            error.append("Player's name is missing, please check.\n");
            return false;
        }

        if (GameServer.players.get(playerName) == null) {
            if (checkPlayerName()) {
                GameServer.players.put(playerName, new Players(playerName, ""));
                GameServer.players.get(playerName).startLocation = GameServer.startLocation;
                GameServer.players.get(playerName).currentLocation = GameServer.startLocation;
                GameServer.locationList.get(GameServer.startLocation).playersAtHere.
                        put(playerName, GameServer.players.get(playerName));
            } else {
                error.append("Player's name is invalid, please check.\n");
                return false;
            }
        }

        GameServer.currentLocation = GameServer.locationList.
                get(GameServer.players.get(playerName).currentLocation);
        checkAction(command);
        checkArtefact(command);
        checkCharacter(command);
        checkFurniture(command);
        checkDestination(command);

        if (actionCnt == 0) {
            error.append("There is no action in the command, please check.\n");
            return false;
        }
        if (actionCnt > 1) {
            addError();
            return false;
        }
        if (!commandIsAvailable()) {
            return false;
        }
        return true;
    }

    public String errorMessage() {
        return error.toString();
    }

    public String commandExtractor(String command) {
        String commandAfterExtract = command;
        int colonIndex = getPlayerName(command);
        StringBuffer tempStr = new StringBuffer();

        if (colonIndex != 0) {
            for (int i = colonIndex + 1; i < command.length(); i++) {
                char ch = command.charAt(i);
                tempStr.append(ch);
            }
            commandAfterExtract = tempStr.toString();
        }

        return commandAfterExtract.toLowerCase();
    }

    public int getPlayerName(String command) {
        int colonIndex = 0;
        if (!command.isEmpty()) {
            char ch = command.charAt(colonIndex);

            while (ch != ':') {
                colonIndex++;
                if (colonIndex == command.length()) {
                    break;
                }
                ch = command.charAt(colonIndex);
            }
            StringBuffer tempStr = new StringBuffer();
            for (int i = 0; i < colonIndex; i++) {
                char c = command.charAt(i);
                tempStr.append(c);
            }
            playerName = tempStr.toString();
        }

        return colonIndex;
    }

    public boolean checkPlayerName() {
        if (!playerName.isEmpty()) {
            for (int i = 0; i < playerName.length(); i++) {
                char ch = playerName.charAt(i);
                if (!Character.isLetter(ch) && ch != ' ' && ch != '\'' && ch != '-') {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void checkAction(String command) {
        for (String tempAction : basicAction) {
            if (command.contains(tempAction)) {
                actionCnt++;
                action = tempAction;
            }
        }
        for (Map.Entry<String, HashSet<GameAction>> entry : GameServer.actions.entrySet()) {
            if (command.contains(entry.getKey())) {
                actionCnt++;
                action = entry.getKey();
            }
        }
    }

    public void checkArtefact(String command) {
        for (Map.Entry<String, Artefacts> entry :
                GameServer.currentLocation.artefactsAtHere.entrySet()) {
            if (command.contains(entry.getKey())) {
                artefactCnt++;
                artefact = entry.getKey();
                entitiesInCommand.add(entry.getKey());
            }
        }
        for (Map.Entry<String, Artefacts> entry :
                GameServer.players.get(playerName).inventory.entrySet()) {
            if (command.contains(entry.getKey())) {
                artefactCnt++;
                artefact = entry.getKey();
                entitiesInCommand.add(entry.getKey());
            }
        }
    }

    public void checkCharacter(String command) {
        for (Map.Entry<String, Characters> entry :
                GameServer.currentLocation.charactersAtHere.entrySet()) {
            if (command.contains(entry.getKey())) {
                characterCnt++;
                character = entry.getKey();
                entitiesInCommand.add(entry.getKey());
            }
        }
    }

    public void checkFurniture(String command) {
        for (Map.Entry<String, Furniture> entry :
                GameServer.currentLocation.furnitureAtHere.entrySet()) {
            if (command.contains(entry.getKey())) {
                furnitureCnt++;
                furniture = entry.getKey();
                entitiesInCommand.add(entry.getKey());
            }
        }
    }

    public void checkDestination(String command) {
        for (Map.Entry<String, Locations> entry :
                GameServer.currentLocation.pathToOtherLocations.entrySet()) {
            if (command.contains(entry.getKey())) {
                destinationCnt++;
                destination = entry.getKey();
                entitiesInCommand.add(entry.getKey());
            }
        }
    }

    public void addError() {
        if (actionCnt > 1) {
            addErrorMessage("action");
        } else if (characterCnt > 1) {
            addErrorMessage("character");
        } else if (furnitureCnt > 1) {
            addErrorMessage("furniture");
        } else {
            addErrorMessage("destination");
        }
    }

    public void addErrorMessage(String errorType) {
        error.append("Too many " + errorType + "s in one command.\n");
        error.append("Only one " + errorType + " is allowed in one command.\n");
    }

    public boolean commandIsAvailable() {
        if (action.equalsIgnoreCase("look") ||
            action.equalsIgnoreCase("inventory") ||
            action.equalsIgnoreCase("inv")) {
            if (artefactCnt != 0 || characterCnt != 0 || furnitureCnt != 0 || destinationCnt != 0) {
                error.append("Too many entities in the command.\n");
                return false;
            }
        }
        else if (action.equalsIgnoreCase("get") ||
                   action.equalsIgnoreCase("drop")) {
            if (artefactCnt == 0) {
                error.append("Artefact missing for action get.\n");
                return false;
            }
            if (characterCnt != 0 || furnitureCnt != 0 || destinationCnt != 0) {
                error.append("Too many entities in the command.\n");
                return false;
            }
        }
        else if (action.equalsIgnoreCase("goto")) {
            if (destinationCnt == 0) {
                error.append("Destination missing/invalid for action goto.\n");
                return false;
            }
            if (artefactCnt != 0 || characterCnt != 0 || furnitureCnt != 0) {
                error.append("Too many entities in the command.\n");
                return false;
            }
        }

        return true;
    }
}
