package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Material {

    /*
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
     */

    private Texture diffuse;

    private Texture specular;

    private float shininess;

    public Material(Texture diffuse, Texture specular, float shininess) {
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    public Texture getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Texture diffuse) {
        this.diffuse = diffuse;
    }

    public Texture getSpecular() {
        return specular;
    }

    public void setSpecular(Texture specular) {
        this.specular = specular;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public List<Texture> getTextures() {
        return Arrays.asList(diffuse, specular);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Material)) return false;
        Material material = (Material) o;
        return Objects.equals(material.diffuse, this.diffuse) &&
                Objects.equals(material.specular, this.specular) &&
                Objects.equals(material.shininess, this.shininess);
    }

    @Override
    public int hashCode() {
        int result = 85;
        return 2 *
                result +
                Objects.hashCode(this.diffuse) +
                Objects.hashCode(this.specular) +
                Objects.hashCode(this.shininess);
    }
}
