package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2f;

public class Sprite {

    private Texture texture;

    private float width;

    private float height;

    private Vector2f[] texCoords = new Vector2f[] {
            new Vector2f(1, 1), // TR
            new Vector2f(1, 0), // BR
            new Vector2f(0, 0), // BL
            new Vector2f(0, 1)  // TL
    };

    public Sprite() {
        this(null);
    }

    public Sprite(Texture texture) {
        this.texture = texture;
    }

    public Vector2f getTexCoords(int index) {
        Vector2f[] uvx = new Vector2f[] {
                texCoords[0], // TR
                texCoords[3], // TL
                texCoords[2], // BL
                texCoords[1], // BR
                texCoords[0], // TR
                texCoords[2]  // BL
        };
        return uvx[index];
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vector2f[] texCoords) {
        this.texCoords = texCoords;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getTexId() {
        return (texture == null) ? -1 : texture.getId();
    }

    @Override
    public String toString() {
        return String.format(
                "Sprite[id: %d, texture :%s, texCords: %s, width: %f, height: %f]",
                getTexId(), texture, Arrays.toString(texCoords), width, height
        );
    }
}
