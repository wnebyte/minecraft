package com.github.wnebyte.minecraft.physics;

import java.util.Objects;
import org.joml.Vector3f;

public class Penetration {

    public final float value;

    public final float abs;

    public final Vector3f axis;

    public Penetration(float value, Vector3f axis) {
        this.value = value;
        this.abs = Math.abs(value);
        this.axis = axis;
    }

    public float getValue() {
        return value;
    }

    public float getAbs() {
        return abs;
    }

    public Vector3f getAxis() {
        return axis;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Penetration)) return false;
        Penetration p = (Penetration)o;
        return Objects.equals(p.value, this.value) &&
                Objects.equals(p.abs, this.abs) &&
                Objects.equals(p.axis, this.axis);
    }

    @Override
    public int hashCode() {
        int result = 98;
        return result +
                Objects.hashCode(this.value) +
                Objects.hashCode(this.abs) +
                Objects.hashCode(this.axis);
    }
}
