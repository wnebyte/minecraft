package com.github.wnebyte.minecraft.util;

import java.util.Objects;
import org.joml.Vector3i;

public class Key {

    private Vector3i v;

    private boolean isBlendable;

    public Key(Vector3i v, boolean isBlendable) {
        this.v = v;
        this.isBlendable = isBlendable;
    }

    public Vector3i getVector3i() {
        return v;
    }

    public boolean isBlendable() {
        return isBlendable;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Key)) return false;
        Key key = (Key) o;
        return Objects.equals(key.v, this.v) &&
                Objects.equals(key.isBlendable, this.isBlendable);
    }

    @Override
    public int hashCode() {
        int result = 75;
        return result +
                Objects.hashCode(this.v) +
                Objects.hashCode(this.isBlendable);
    }

}
