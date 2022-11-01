package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import org.joml.Vector3f;

public class Line3D {

    private Vector3f start;

    private Vector3f end;

    private Vector3f color;

    public Line3D(Vector3f start, Vector3f end, Vector3f color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public Vector3f getStart() {
        return start;
    }

    public void setStart(Vector3f start) {
        this.start = start;
    }

    public Vector3f getEnd() {
        return end;
    }

    public void setEnd(Vector3f end) {
        this.end = end;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Line3D)) return false;
        Line3D line3D = (Line3D) o;
        return Objects.equals(line3D.start, this.start) &&
                Objects.equals(line3D.end, this.end) &&
                Objects.equals(line3D.color, this.color);
    }

    @Override
    public int hashCode() {
        int result = 15;
        return 3 *
                result +
                Objects.hashCode(this.start) +
                Objects.hashCode(this.end) +
                Objects.hashCode(this.color);
    }
}
