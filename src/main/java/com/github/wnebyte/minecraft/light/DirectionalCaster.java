package com.github.wnebyte.minecraft.light;

import org.joml.Vector3f;

public class DirectionalCaster extends Caster {

    private Vector3f direction;

    public DirectionalCaster(Vector3f direction, Light light) {
        this.direction = direction;
        this.light = light;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
}
