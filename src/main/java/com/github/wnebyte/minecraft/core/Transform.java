package com.github.wnebyte.minecraft.core;

import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Transform {

    /*
    ###########################
    #       STATIC FIELDS     #
    ###########################
    */

    private static final float DEFAULT_ROTATION = 0.0f;

    /*
    ###########################
    #         FIELDS          #
    ###########################
    */

    public final Vector3f position;

    public final Vector3f scale;

    public float rotation;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Transform() {
        this(new Vector3f(), new Vector3f());
    }

    public Transform(Vector3f position) {
        this(position, new Vector3f());
    }

    public Transform(Vector3f position, Vector3f scale) {
        this(position, scale, DEFAULT_ROTATION);
    }

    public Transform(Vector3f position, Vector3f scale, float rotation) {
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    public Transform copy() {
        return new Transform(new Vector3f(position), new Vector3f(scale), rotation);
    }

    public void copyInto(Transform transform) {
        transform.position.set(this.position);
        transform.scale.set(this.scale);
        transform.rotation = this.rotation;
    }

    public Matrix4f getTransformMatrix() {
        Matrix4f transformMatrix = new Matrix4f().identity();
        transformMatrix.translate(position.x, position.y, position.z);
        transformMatrix.rotate((float)Math.toRadians(rotation), 0, 0, 1);
        transformMatrix.scale(scale.x, scale.y, scale.z);
        return transformMatrix;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Transform)) return false;
        Transform transform = (Transform) o;
        return Objects.equals(transform.position, this.position) &&
                Objects.equals(transform.scale, this.scale) &&
                Objects.equals(transform.rotation, this.rotation) &&
                super.equals(transform);
    }

    @Override
    public int hashCode() {
        int result = 79;
        return result +
                13 +
                Objects.hashCode(this.position) +
                Objects.hashCode(this.scale) +
                Objects.hashCode(this.rotation) +
                super.hashCode();
    }

    @Override
    public String toString() {
        return String.format(
                "Transform[position: %s, scale: %s, rotation: %.2f]", position, scale, rotation
        );
    }
}
