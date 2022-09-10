package com.github.wnebyte.minecraft.light;

import java.util.Objects;
import org.joml.Vector3f;

public class Light {

    private Vector3f ambient;

    private Vector3f diffuse;

    private Vector3f specular;

    public Light() {}

    public Light(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    public Vector3f getAmbient() {
        return ambient;
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }

    public Vector3f getSpecular() {
        return specular;
    }

    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Caster)) return false;
        Light light = (Light) o;
        return Objects.equals(light.ambient, this.ambient) &&
                Objects.equals(light.diffuse, this.diffuse) &&
                Objects.equals(light.specular, this.specular);
    }

    @Override
    public int hashCode() {
        int result = 4;
        return 3 *
                result +
                Objects.hashCode(this.ambient) +
                Objects.hashCode(this.diffuse) +
                Objects.hashCode(this.specular);
    }
}
