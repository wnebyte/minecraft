package com.github.wnebyte.minecraft.event;

public interface EventHandler<T extends Event> {

    void handle(T event);
}
