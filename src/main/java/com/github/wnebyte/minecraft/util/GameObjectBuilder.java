package com.github.wnebyte.minecraft.util;

import java.util.List;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;

public class GameObjectBuilder {

    private String name;

    private Transform transform;

    private List<Component> components;

    public GameObjectBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public GameObjectBuilder setTransform(Transform transform) {
        this.transform = transform;
        return this;
    }

    public GameObjectBuilder setComponents(List<Component> components) {
        this.components = components;
        return this;
    }

    public GameObjectBuilder addComponent(Component c) {
        this.components.add(c);
        return this;
    }

    public GameObject build() {
        GameObject go = new GameObject((name != null) ? name : "Gen");
        go.addComponent(transform);
        go.transform = transform;
        for (Component c : components) {
            go.addComponent(c);
        }
        return go;
    }
}
