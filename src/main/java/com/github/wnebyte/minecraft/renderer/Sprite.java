package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2f;

public class Sprite {

    public static class Configuration {

        private Vector2f start;

        private Vector2f size;

        public Configuration(Vector2f start, Vector2f size) {
            this.start = start;
            this.size = size;
        }

        public Vector2f getStart() {
            return start;
        }

        public Vector2f getSize() {
            return size;
        }

        public static class Builder {

            private Vector2f start, size;

            public Builder setStart(Vector2f start) {
                this.start = start;
                return this;
            }

            public Builder setSize(Vector2f size) {
                this.size = size;
                return this;
            }

            public Configuration build() {
                return new Configuration(start, size);
            }
        }
    }

    private Texture texture;

    private float width;

    private float height;

    private Vector2f[] texCoords;

    public Sprite() {
        this(null);
    }

    public Sprite(Texture texture) {
        this.texture = texture;
        this.texCoords = new Vector2f[] {
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),
                new Vector2f(0, 1)};
    }

    public void invertTexCoords() {
        if (texCoords.length >= 4) {
            Vector2f tr = texCoords[0];
            Vector2f br = texCoords[1];
            Vector2f bl = texCoords[2];
            Vector2f tl = texCoords[3];
            texCoords[0] = br;
            texCoords[1] = tr;
            texCoords[2] = tl;
            texCoords[3] = bl;
        }
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
        return String.format("Sprite[texture: %s, width: %.2f, height: %.2f, texCoords: %s]",
                texture, width, height, Arrays.toString(texCoords));
    }
}
