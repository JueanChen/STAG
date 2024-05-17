package edu.uob;

import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

public class EntityReader {
    public EntityReader() {}

    public void readEntitiesFromFile(File entitiesFile) {
        Parser parser = new Parser();
        FileReader reader;
        try {
            reader = new FileReader(entitiesFile);
            parser.parse(reader);
        } catch (FileNotFoundException | ParseException e) {
            throw new RuntimeException(e);
        }
        Graph wholeDocument = parser.getGraphs().get(0);
        ArrayList<Graph> sections = wholeDocument.getSubgraphs();

        // Read & write all the locations to the locationList
        addLocationsToList(sections);

        // Read & write all the paths to the locationList
        addPathsToList(sections);

    }

    void addLocationsToList(ArrayList<Graph> sections) {
        ArrayList<Graph> locations = sections.get(0).getSubgraphs();
        for (int i = 0; i < locations.size(); i++) {
            Graph tempLocation = locations.get(i);
            Node locationDetails = tempLocation.getNodes(false).get(0);
            String locationName = locationDetails.getId().getId();
            String locationDescription = locationDetails.getAttribute("description");
            GameServer.locationList.put(locationName, new Locations(locationName, locationDescription));

            // Read & write all the entities at this location to the current location's entity lists
            ArrayList<Graph> entities = tempLocation.getSubgraphs();
            addEntitiesToLocation(locationName, entities);

            if (i == 0) {
                GameServer.startLocation = locationName;
                GameServer.currentLocation  = GameServer.locationList.get(GameServer.startLocation);
            }
        }
    }

    void addEntitiesToLocation(String locationName, ArrayList<Graph> entities) {
        for (int j = 0; j < entities.size(); j++) {
            Graph tempEntity = entities.get(j);
            String entityType = tempEntity.getId().getId();
            ArrayList<Node> entityDetails = tempEntity.getNodes(false);
            for (int k = 0; k < entityDetails.size(); k++) {
                Node tempNode = entityDetails.get(k);
                String entityName = tempNode.getId().getId();
                String entityDescription = tempNode.getAttribute("description");
                addEntityToList(locationName, entityType, entityName, entityDescription);
            }
        }
    }

    void addEntityToList(String locationName, String entityType, String name, String description) {
        if (entityType.equalsIgnoreCase("artefacts")) {
            GameServer.locationList.get(locationName).artefactsAtHere.put(name, new Artefacts(name, description));
        } else if (entityType.equalsIgnoreCase("furniture")) {
            GameServer.locationList.get(locationName).furnitureAtHere.put(name, new Furniture(name, description));
        } else {
            GameServer.locationList.get(locationName).charactersAtHere.put(name, new Characters(name, description));
        }
    }

    void addPathsToList(ArrayList<Graph> sections) {
        ArrayList<Edge> paths = sections.get(1).getEdges();
        for (int i = 0; i < paths.size(); i++) {
            Edge firstPath = paths.get(i);
            Node fromLocation = firstPath.getSource().getNode();
            String fromName = fromLocation.getId().getId();
            Node toLocation = firstPath.getTarget().getNode();
            String toName = toLocation.getId().getId();
            GameServer.locationList.get(fromName).pathToOtherLocations.put(toName, GameServer.locationList.get(toName));
        }
    }
}
