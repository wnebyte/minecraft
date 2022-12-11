package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex2D {

    private Vector2f position;

    private int zIndex;

    private Vector3f color;

    private Vector2f texCoords;

    private int texId;

    private boolean blend;

    public Vertex2D(Vector2f position, int zIndex, Vector3f color, Vector2f texCoords, int texId, boolean blend) {
        this.position = position;
        this.zIndex = zIndex;
        this.color = color;
        this.texCoords = texCoords;
        this.texId = texId;
        this.blend = blend;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector2f getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vector2f texCoords) {
        this.texCoords = texCoords;
    }

    public int getTexId() {
        return texId;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    public boolean isBlend() {
        return blend;
    }

    public void setBlend(boolean blend) {
        this.blend = blend;
    }
}
