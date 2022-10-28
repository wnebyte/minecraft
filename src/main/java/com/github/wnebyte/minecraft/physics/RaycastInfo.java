package com.github.wnebyte.minecraft.physics;

import org.joml.Vector3f;

public class RaycastInfo {

    public Vector3f point;

    public Vector3f blockCenter;

    public Vector3f blockSize;

    public Vector3f contactNormal;

    public boolean hit;

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getBlockCenter() {
        return blockCenter;
    }

    public Vector3f getBlockSize() {
        return blockSize;
    }

    public Vector3f getContactNormal() {
        return contactNormal;
    }

    public boolean isHit() {
        return hit;
    }
}
