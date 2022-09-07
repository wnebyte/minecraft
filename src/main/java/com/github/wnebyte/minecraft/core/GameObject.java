package com.github.wnebyte.minecraft.core;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class GameObject {

    public void init(int maxId) {
        ID_COUNTER = maxId;
    }

    private static int ID_COUNTER = 0;

    private int id = -1;

    private String name;

    private final List<Component> components;

    public GameObject(String name) {
        this.name = name;
        this.components = new ArrayList<>();
    }

    public void generateId() {
        if (id == -1) {
            id = ID_COUNTER++;
        }
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : String.format("Error: (GameObject) Casting Component: '%s'", c.getClass());
                }
            }
        }

        return null;
    }

    public <T extends Component> List<T> getComponents(Class<T> componentClass) {
        List<T> myComponents = new ArrayList<>();
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    T myComponent = componentClass.cast(c);
                    myComponents.add(myComponent);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : String.format("Error: (GameObject) Casting Component: '%s'", c.getClass());
                }
            }
        }

        return myComponents;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public <T extends Component> void removeComponents(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                i--;
            }
        }
    }

    public void addComponent(Component c) {
        components.add(c);
        c.gameObject = this;
    }

    public List<Component> getAllComponents() {
        return components;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof GameObject)) return false;
        GameObject go = (GameObject) o;
        return Objects.equals(go.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 76;
        return result +
                13 +
                Objects.hashCode(this.id);
    }

}
