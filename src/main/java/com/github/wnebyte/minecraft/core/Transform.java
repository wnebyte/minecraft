package com.github.wnebyte.minecraft.core;

import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;

public class Transform extends Component {

    public static Transform copy(Transform transform) {
        return new Transform(new Vector3f(transform.position), new Vector3f(transform.scale), transform.rotation);
    }

    /*
    ###########################
    #       STATIC FIELDS     #
    ###########################
    */

    private static final Vector4f DEFAULT_ROTATION = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    /*
    ###########################
    #         FIELDS          #
    ###########################
    */

    public final Vector3f position;

    public final Vector3f scale;

    public final Vector4f rotation;

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

    public Transform(Vector3f position, Vector3f scale, Vector4f rotation) {
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    public Matrix4f toMat4f() {
        Matrix4f transformMatrix = new Matrix4f().identity();
        transformMatrix.translate(position.x, position.y, position.z);
        transformMatrix.rotate((float)Math.toRadians(rotation.x), rotation.y, rotation.z, rotation.w);
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
                "Transform[position: %s, scale: %s, rotation: %s]", position, scale, rotation
        );
    }
}
