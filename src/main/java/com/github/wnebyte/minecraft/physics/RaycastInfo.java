package com.github.wnebyte.minecraft.physics;

import org.joml.Vector3f;

public class RaycastInfo {

    public Vector3f point;

    public Vector3f center;

    public Vector3f size;

    public Vector3f contactNormal;

    public boolean hit;

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getCenter() {
        return center;
    }

    public Vector3f getSize() {
        return size;
    }

    public Vector3f getContactNormal() {
        return contactNormal;
    }

    public boolean isHit() {
        return hit;
    }
}
