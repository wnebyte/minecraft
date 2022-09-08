package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import org.joml.Vector3f;

public class Material {

    public static final Material DEFAULT = new Material(
            new Vector3f(1.0f, 0.5f, 0.31f),
            new Vector3f(1.0f, 0.5f, 0.31f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            32.0f
    );

    public static final Material CYAN_PLASTIC = new Material(
            new Vector3f(0.0f, 0.1f, 0.06f),
            new Vector3f(0.0f, 0.50980392f, 0.50980392f),
            new Vector3f(0.50196078f, 0.50196078f, 0.50196078f),
            32.0f
    );

    private Vector3f ambient;

    private Vector3f diffuse;

    private Vector3f specular;

    private float shininess;

    public Material() {

    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, float shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
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

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Material)) return false;
        Material material = (Material) o;
        return Objects.equals(material.ambient, this.ambient) &&
                Objects.equals(material.diffuse, this.diffuse) &&
                Objects.equals(material.specular, this.specular) &&
                Objects.equals(material.shininess, this.shininess);
    }

    @Override
    public int hashCode() {
        int result = 85;
        return 2 *
                result +
                Objects.hashCode(this.ambient) +
                Objects.hashCode(this.diffuse) +
                Objects.hashCode(this.specular) +
                Objects.hashCode(this.shininess);
    }

    @Override
    public String toString() {
        return String.format("Material[ambient: %s, diffuse: %s, specular: %s shininess: %.2f]",
                this.ambient, this.diffuse, this.specular, this.shininess);
    }
}
