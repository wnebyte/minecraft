package com.github.wnebyte.minecraft.renderer.fonts;

import org.joml.Vector2f;

public class CharInfo {

    private int sourceX;

    private int sourceY;

    private int decent;

    private int width;

    private int height;

    private Vector2f[] texCoords = new Vector2f[4];

    public CharInfo(int sourceX, int sourceY, int decent, int width, int height) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.decent = decent;
        this.width = width;
        this.height = height;
    }

    public void calculateTexCoords(int fontWidth, int fontHeight) {
        float x0 = (float)(sourceX) / (float)fontWidth;
        float x1 = (float)(sourceX + width) / (float)fontWidth;
        float y0 = (float)(sourceY - height)/ (float) fontHeight;
        float y1 = ((float)(sourceY) / (float)fontHeight) + ((float)decent / fontHeight);

        texCoords[0] = new Vector2f(x0, y1);
        texCoords[1] = new Vector2f(x1, y0);
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    public int getDecent() {
        return decent;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public static class Builder {

        private int sourceX;

        private int sourceY;

        private int decent;

        private int width;

        private int height;

        public Builder setSourceX(int sourceX) {
            this.sourceX = sourceX;
            return this;
        }

        public Builder setSourceY(int sourceY) {
            this.sourceY = sourceY;
            return this;
        }

        public Builder setDecent(int decent) {
            this.decent = decent;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public CharInfo build() {
            return new CharInfo(sourceX, sourceY, decent, width, height);
        }

    }
}
