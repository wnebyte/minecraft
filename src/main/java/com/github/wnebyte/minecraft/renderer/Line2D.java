package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line2D {

    private Vector2f start;

    private Vector2f end;

    private Vector3f color;

    private int ftl;

    public Line2D(Vector2f start, Vector2f end, Vector3f color) {
        this(start, end, color, Integer.MAX_VALUE);
    }

    public Line2D(Vector2f start, Vector2f end, Vector3f color, int ftl) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.ftl = ftl;
    }

    public int beginFrame() {
        ftl--;
        return ftl;
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

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public int getFtl() {
        return ftl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Line2D)) return false;
        Line2D line2D = (Line2D) o;
        return Objects.equals(line2D.start, this.start) &&
                Objects.equals(line2D.end, this.end) &&
                Objects.equals(line2D.color, this.color) &&
                Objects.equals(line2D.ftl, this.ftl);
    }

    @Override
    public int hashCode() {
        int result = 15;
        return 3 *
                result +
                Objects.hashCode(this.start) +
                Objects.hashCode(this.end) +
                Objects.hashCode(this.color) +
                Objects.hashCode(this.ftl);
    }
}
