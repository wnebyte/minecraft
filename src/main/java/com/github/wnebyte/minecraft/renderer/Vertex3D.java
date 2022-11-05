package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex3D {

    private Vector3f position;

    private Vector3f color;

    private Vector2f texCoords;

    private int texId;

    public Vertex3D(Vector3f position, Vector3f color, Vector2f texCoords, int texId) {
        this.position = position;
        this.color = color;
        this.texCoords = texCoords;
        this.texId = texId;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
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
}
