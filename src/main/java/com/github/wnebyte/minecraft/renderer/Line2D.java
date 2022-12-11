package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line2D {

    public static final float DEFAULT_WIDTH = 2.0f;

    private Vector2f start;

    private Vector2f end;

    private int zIndex;

    private Vector3f color;

    private float width;

    public Line2D(Vector2f start, Vector2f end, int zIndex, Vector3f color) {
        this(start, end, zIndex, color, DEFAULT_WIDTH);
    }

    public Line2D(Vector2f start, Vector2f end, int zIndex, Vector3f color, float width) {
        this.start = start;
        this.zIndex = zIndex;
        this.end = end;
        this.color = color;
        this.width = width;
    }

    public Vector2f getStart() {
        return start;
    }

    public void setStart(Vector2f start) {
        this.start = start;
    }

    public Vector2f getEnd() {
        return end;
    }

    public void setEnd(Vector2f end) {
        this.end = end;
    }

    public int getZIndex() {
        return zIndex;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Line2D)) return false;
        Line2D line2D = (Line2D) o;
        return Objects.equals(line2D.start, this.start) &&
                Objects.equals(line2D.end, this.end) &&
                Objects.equals(line2D.zIndex, this.zIndex) &&
                Objects.equals(line2D.color, this.color) &&
                Objects.equals(line2D.width, this.width);
    }

    @Override
    public int hashCode() {
        int result = 15;
        return 3 *
                result +
                Objects.hashCode(this.start) +
                Objects.hashCode(this.end) +
                Objects.hashCode(this.zIndex) +
                Objects.hashCode(this.color) +
                Objects.hashCode(this.width);
    }
}
