package com.github.wnebyte.minecraft.ui;

import org.joml.Vector2f;
import com.github.wnebyte.minecraft.renderer.Sprite;

public class JImage {

    public static class Builder {

        private float width;

        private float height;

        private Sprite sprite;

        private int rgb = 0xFFFFFF;

        private int zIndex;

        public Builder setSize(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        public Builder setSprite(Sprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public Builder setRGB(int rgb) {
            this.rgb = rgb;
            return this;
        }

        public Builder setZIndex(int zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        public JImage build() {
            return new JImage(width, height, sprite, rgb, zIndex);
        }
    }

    private Vector2f size;

    private Sprite sprite;

    private int rgb;

    private int zIndex;

    public JImage(float width, float height, Sprite sprite, int rgb, int zIndex) {
        this.size = new Vector2f(width, height);
        this.sprite = sprite;
        this.rgb = rgb;
        this.zIndex = zIndex;
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public int getRGB() {
        return rgb;
    }

    public void setRGB(int rgb) {
        this.rgb = rgb;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }
}
