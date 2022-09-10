package com.github.wnebyte.minecraft.util;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.renderer.Material;
import com.github.wnebyte.minecraft.renderer.Texture;

public class MaterialBuilder {

    private Texture diffuseMap = null;

    private Texture specularMap = null;

    private Vector3f diffuseColor = null;

    private Vector3f specularColor = null;

    private float shininess = 0.0f;

    private String diffuseMapPath = null;

    private String specularMapPath = null;

    public MaterialBuilder setDiffuseMap(Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
        return this;
    }

    public MaterialBuilder setSpecularMap(Texture specularMap) {
        this.specularMap = specularMap;
        return this;
    }

    public MaterialBuilder setSpecularColor(Vector3f specularColor) {
        this.specularColor = specularColor;
        return this;
    }

    public MaterialBuilder setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
        return this;
    }

    public MaterialBuilder setShininess(float shininess) {
        this.shininess = shininess;
        return this;
    }

    public MaterialBuilder setDiffuseMapPath(String diffuseMapPath) {
        this.diffuseMapPath = diffuseMapPath;
        return this;
    }

    public MaterialBuilder setSpecularMapPath(String specularMapPath) {
        this.specularMapPath = specularMapPath;
        return this;
    }

    public Material build() {
        if (diffuseMapPath != null) {
            diffuseMap = Assets.getTexture(diffuseMapPath);
        }
        if (specularMapPath != null) {
            specularMap = Assets.getTexture(specularMapPath);
        }
        if (diffuseMap != null && specularMap != null) {
            return new Material(diffuseMap, specularMap, shininess);
        } else if (diffuseColor != null && specularColor != null) {
            return new Material(diffuseColor, specularColor, shininess);
        } else {
            throw new IllegalArgumentException(
                    ""
            );
        }
    }
}
