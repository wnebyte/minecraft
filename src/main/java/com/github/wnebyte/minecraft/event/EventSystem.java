package com.github.wnebyte.minecraft.event;

import java.util.List;
import java.util.ArrayList;

public class EventSystem {

    private static final List<Observer> observers = new ArrayList<>();

    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public static void notify(Event event) {
        for (Observer observer : observers) {
            observer.notify(event);
        }
    }
}
