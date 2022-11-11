package com.github.wnebyte.minecraft.renderer.fonts;

import java.util.Arrays;
import java.util.Objects;
import org.joml.Vector2f;

public class CharInfo {

    private float sourceX;

    private float sourceY;

    private int decent;

    private int ascent;

    private int width;

    private int height;

    private Vector2f[] texCoords = new Vector2f[4];

    public CharInfo(float sourceX, float sourceY, int decent, int ascent, int width, int height) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.decent = decent;
        this.ascent = ascent;
        this.width = width;
        this.height = height;
    }

    public void calculateTexCoords(int fontWidth, int fontHeight) {
        float x0 = sourceX / (float)fontWidth;
        float x1 = (sourceX + width) / (float)fontWidth;
        float y0 = (sourceY - height) / (float) fontHeight;
        float y1 = (sourceY / (float)fontHeight) + ((float)decent / fontHeight);

        texCoords[0] = new Vector2f(x0, y1);
        texCoords[1] = new Vector2f(x1, y0);
    }

    public float getSourceX() {
        return sourceX;
    }

    public float getSourceY() {
        return sourceY;
    }

    public int getDecent() {
        return decent;
    }

    public int getAscent() {
        return ascent;
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof CharInfo)) return false;
        CharInfo info = (CharInfo) o;
        return Objects.equals(info.sourceX, this.sourceX) &&
                Objects.equals(info.sourceY, this.sourceY) &&
                Arrays.equals(info.texCoords, this.texCoords);
    }

    @Override
    public int hashCode() {
        int result = 53;
        return result +
                Objects.hashCode(this.sourceX) +
                Objects.hashCode(this.sourceY) +
                Arrays.hashCode(this.texCoords);
    }

    public static class Builder {

        private float sourceX;

        private float sourceY;

        private int decent;

        private int ascent;

        private int width;

        private int height;

        public Builder setSourceX(float sourceX) {
            this.sourceX = sourceX;
            return this;
        }

        public Builder setSourceY(float sourceY) {
            this.sourceY = sourceY;
            return this;
        }

        public Builder setDecent(int decent) {
            this.decent = decent;
            return this;
        }

        public Builder setAscent(int ascent) {
            this.ascent = ascent;
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
            return new CharInfo(sourceX, sourceY, decent, ascent, width, height);
        }

    }
}
