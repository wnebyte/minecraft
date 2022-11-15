package com.github.wnebyte.minecraft.components;

import java.util.Map;
import java.util.HashMap;

public class KeyBindings {

    private final Map<KeyBinding, Integer> map;

    public KeyBindings() {
        this.map = new HashMap<>();
    }

    public void put(KeyBinding key, int value) {
        map.put(key, value);
    }

    public void putAll(Map<? extends KeyBinding, ? extends Integer> m) {
        map.putAll(m);
    }

    public int get(KeyBinding key) {
        return map.get(key);
    }
}
