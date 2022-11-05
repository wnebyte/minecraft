package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Transform;

public class Cube3D {

    private Transform transform;

    private Vector3f color;

    private Sprite sideSprite;

    private Sprite topSprite;

    private Sprite bottomSprite;


    public Cube3D(Transform transform, Vector3f color, Sprite sideSprite, Sprite topSprite, Sprite bottomSprite) {
        this.transform = transform;
        this.color = color;
        this.sideSprite = sideSprite;
        this.topSprite = topSprite;
        this.bottomSprite = bottomSprite;
    }

    public Transform getTransform() {
        return transform;
    }

    public Vector3f getColor() {
        return color;
    }

    public Sprite getSideSprite() {
        return sideSprite;
    }

    public void setSideSprite(Sprite sideSprite) {
        this.sideSprite = sideSprite;
    }

    public Sprite getTopSprite() {
        return topSprite;
    }

    public void setTopSprite(Sprite topSprite) {
        this.topSprite = topSprite;
    }

    public Sprite getBottomSprite() {
        return bottomSprite;
    }

    public void setBottomSprite(Sprite bottomSprite) {
        this.bottomSprite = bottomSprite;
    }

    public void setColor(Vector3f value) {
        if (!color.equals(value)) {
            color.set(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Cube3D)) return false;
        Cube3D c = (Cube3D) o;
        return Objects.equals(c.transform, this.transform) &&
                Objects.equals(c.color, this.color);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.transform) +
                Objects.hashCode(this.color);
    }
}
