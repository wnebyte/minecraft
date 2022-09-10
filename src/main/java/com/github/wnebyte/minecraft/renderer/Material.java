package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.joml.Vector3f;
import org.joml.Vector4f;

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

    private static final Vector3f DEFAULT_DIFFUSE_COLOR =
            new Vector3f(1.0f, 1.0f, 1.0f);

    private static final Vector3f DEFAULT_SPECULAR_COLOR =
            new Vector3f(1.0f, 1.f, 1.f);

    private Texture diffuseMap;

    private Texture specularMap;

    private Vector3f diffuseColor;

    private Vector3f specularColor;

    private float shininess;

    public Material(Texture diffuseMap, Texture specularMap, float shininess) {
        this.diffuseMap = diffuseMap;
        this.specularMap = specularMap;
        this.diffuseColor = new Vector3f(DEFAULT_DIFFUSE_COLOR);
        this.specularColor = new Vector3f(DEFAULT_SPECULAR_COLOR);
        this.shininess = shininess;
    }

    public Material(Vector3f diffuseColor, Vector3f specularColor, float shininess) {
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
    }

    public Texture getDiffuseMap() {
        return diffuseMap;
    }

    public void setDiffuseMap(Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
    }

    public Texture getSpecularMap() {
        return specularMap;
    }

    public void setSpecularMap(Texture specularMap) {
        this.specularMap = specularMap;
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Vector3f getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Vector3f specularColor) {
        this.specularColor = specularColor;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public List<Texture> getTextures() {
        return Arrays.asList(diffuseMap, specularMap);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Material)) return false;
        Material material = (Material) o;
        return Objects.equals(material.diffuseMap, this.diffuseMap) &&
                Objects.equals(material.specularMap, this.specularMap) &&
                Objects.equals(material.diffuseColor, this.diffuseColor) &&
                Objects.equals(material.specularColor, this.specularColor) &&
                Objects.equals(material.shininess, this.shininess);
    }

    @Override
    public int hashCode() {
        int result = 85;
        return 2 *
                result +
                Objects.hashCode(this.diffuseMap) +
                Objects.hashCode(this.specularMap) +
                Objects.hashCode(this.diffuseColor) +
                Objects.hashCode(this.specularColor) +
                Objects.hashCode(this.shininess);
    }
}
