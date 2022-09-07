package com.github.wnebyte.minecraft.core;

import java.util.Objects;

public abstract class Component {

    public void init(int maxId) {
        ID_COUNTER = maxId;
    }

    private static int ID_COUNTER = 0;

    public transient GameObject gameObject;

    private int id = -1;

    public void start() {}

    public void update() {}

    public void destroy() {}

    public void generateId() {
        if (id == -1) {
            id = ID_COUNTER++;
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Component)) return false;
        Component c = (Component) o;
        return Objects.equals(c.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 33;
        return result +
                17 +
                Objects.hashCode(this.id);
    }
}
