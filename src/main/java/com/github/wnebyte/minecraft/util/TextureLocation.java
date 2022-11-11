package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class TextureLocation {

    private final String name;

    private final float x;

    private final float y;

    private final int width;

    private final int height;

    public TextureLocation(String name, int x, int y, int width, int height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof TextureLocation)) return false;
        TextureLocation location = (TextureLocation) o;
        return Objects.equals(location.name, this.name) &&
                Objects.equals(location.x, this.x) &&
                Objects.equals(location.y, this.y) &&
                Objects.equals(location.width, this.width) &&
                Objects.equals(location.height, this.height);
    }

    @Override
    public int hashCode() {
        int result = 12;
        return result +
                Objects.hashCode(this.name) +
                Objects.hashCode(this.x) +
                Objects.hashCode(this.y) +
                Objects.hashCode(this.width) +
                Objects.hashCode(this.height);
    }
}
