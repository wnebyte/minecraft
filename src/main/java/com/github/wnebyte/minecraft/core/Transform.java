package com.github.wnebyte.minecraft.core;

import java.util.Arrays;
import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;

public class Transform extends Component {

    public static Transform copy(Transform transform) {
        return new Transform(new Vector3f(transform.position), new Vector3f(transform.scale), transform.rotations);
    }

    /*
    ###########################
    #       STATIC FIELDS     #
    ###########################
    */

    /*
    ###########################
    #         FIELDS          #
    ###########################
    */

    public final Vector3f position;

    public final Vector3f scale;

    public final Vector4f[] rotations;

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
        this(position, scale, new Vector4f[]{});
    }

    public Transform(Vector3f position, Vector3f scale, Vector4f[] rotations) {
        this.position = position;
        this.scale = scale;
        this.rotations = rotations;
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    public Matrix4f toMat4f() {
        Matrix4f transformMatrix = new Matrix4f().identity();
        transformMatrix.translate(position.x, position.y, position.z);
        for (Vector4f rotation : rotations) {
            if (rotation != null) {
                transformMatrix.rotate((float)Math.toRadians(rotation.x), rotation.y, rotation.z, rotation.w);
            }
        }
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
                Arrays.equals(transform.rotations, this.rotations);
    }

    @Override
    public int hashCode() {
        int result = 79;
        return result +
                13 +
                Objects.hashCode(this.position) +
                Objects.hashCode(this.scale) +
                Arrays.hashCode(this.rotations);
    }

    @Override
    public String toString() {
        return String.format(
                "Transform[position: %s, scale: %s, rotations: %s]",
                this.position, this.scale, Arrays.toString(this.rotations)
        );
    }
}
