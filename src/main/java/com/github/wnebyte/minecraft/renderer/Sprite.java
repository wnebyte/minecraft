package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2f;

public class Sprite {

    public static class Configuration {

        private String name;

        private Vector2f start;

        private Vector2f size;

        public Configuration(Vector2f start, Vector2f size) {
            this(null, start, size);
        }

        public Configuration(String name, Vector2f start, Vector2f size) {
            this.name = name;
            this.start = start;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public Vector2f getStart() {
            return start;
        }

        public Vector2f getSize() {
            return size;
        }

        public static class Builder {

            private String name;

            private Vector2f start, size;

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setStart(Vector2f start) {
                this.start = start;
                return this;
            }

            public Builder setSize(Vector2f size) {
                this.size = size;
                return this;
            }

            public Configuration build() {
                return new Configuration(name, start, size);
            }
        }
    }

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
        String uvString = String.format(
                "TR: [x: %.2f, y: %.2f], BR: [x: %.2f, y: %.2f], BL: [x: %.2f, y: %.2f], TL: [x: %.2f, y: %.2f]",
                texCoords[0].x, texCoords[0].y,
                texCoords[1].x, texCoords[1].y,
                texCoords[2].x, texCoords[2].y,
                texCoords[3].x, texCoords[3].y);
        return String.format(
                "Sprite[id: %d, texture :%s, texCords: [%s], width: %f, height: %f]",
                getTexId(), texture, uvString, width, height
        );
    }
}
