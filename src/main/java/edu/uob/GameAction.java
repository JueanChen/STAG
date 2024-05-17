package edu.uob;

import java.util.HashSet;

public class GameAction {
    int entitiesRequiredCnt;
    HashSet<String> entitiesRequired;
    HashSet<String> entitiesConsumed;
    HashSet<String> entitiesProduced;
    String narration;
    public GameAction() {
        entitiesRequiredCnt = 0;
        entitiesRequired = new HashSet<>();
        entitiesConsumed = new HashSet<>();
        entitiesProduced = new HashSet<>();
    }

    public String actionSucceeded() {
        return narration;
    }

    public void print() {
        System.out.print("Subjects: ");
        for(String str : entitiesRequired) {
            System.out.print(str + " ");
        }
        System.out.println();
        System.out.print("Consumed: ");
        for(String str : entitiesConsumed) {
            System.out.print(str + " ");
        }
        System.out.println();
        System.out.print("Produced: ");
        for(String str : entitiesProduced) {
            System.out.print(str + " ");
        }
        System.out.println();
        System.out.println("Narration: " + actionSucceeded());
    }
}
