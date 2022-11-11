package com.github.wnebyte.minecraft.event;

import java.util.List;
import java.util.ArrayList;

public class EventSystem {

    private final List<Observer> observers;

    public EventSystem() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notify(Event event) {
        for (Observer observer : observers) {
            observer.notify(event);
        }
    }
}
