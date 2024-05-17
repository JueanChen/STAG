package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ActionReader {
    public GameAction gameAction = new GameAction();
    public ActionReader() {}
    public void readActionsFromFile(File actionsFile) {
        DocumentBuilder builder;
        Document document;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(actionsFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        // Add actions from actionsFile to actions
        addActionsToList(document);
    }

    void addActionsToList(Document document) {
        Element root = document.getDocumentElement();
        NodeList actionList = root.getChildNodes();

        // Get action (only the odd items are actually actions: 1, 3, 5 etc.)
        for (int i = 1; i < actionList.getLength(); i += 2) {
            Element tempAction = (Element)actionList.item(i);
            Element triggers = (Element)tempAction.getElementsByTagName("triggers").item(0);
            // Get trigger phrase
            for (int j = 0; j < triggers.getElementsByTagName("keyphrase").getLength(); j++) {
                String triggerPhrase = triggers.getElementsByTagName("keyphrase").item(j).getTextContent();
                GameServer.actions.computeIfAbsent(triggerPhrase, k -> new HashSet<>());
                // Get the rest of GameAction
                addGameActionsToActions(tempAction, triggerPhrase);
            }
        }
    }

    void addGameActionsToActions(Element tempAction, String triggerPhrase) {
        gameAction = new GameAction();
        addEntitiesToActions(tempAction, "subjects");
        addEntitiesToActions(tempAction, "consumed");
        addEntitiesToActions(tempAction, "produced");
        Element tempElement = (Element)tempAction.getElementsByTagName("narration").item(0);
        gameAction.narration = tempElement.getTextContent();
        GameServer.actions.get(triggerPhrase).add(gameAction);
    }

    void addEntitiesToActions(Element tempAction, String tagName) {
        Element tempElement = (Element)tempAction.getElementsByTagName(tagName).item(0);
        if (tempElement.getElementsByTagName("entity").getLength() > 0) {
            for (int i = 0; i < tempElement.getElementsByTagName("entity").getLength(); i++) {
                String entityName = tempElement.getElementsByTagName("entity").item(i).getTextContent();
                if (tagName.equalsIgnoreCase("subjects")) {
                    gameAction.entitiesRequired.add(entityName);
                    gameAction.entitiesRequiredCnt++;
                } else if (tagName.equalsIgnoreCase("consumed")) {
                    gameAction.entitiesConsumed.add(entityName);
                } else {
                    gameAction.entitiesProduced.add(entityName);
                }
            }
        }
    }
}
