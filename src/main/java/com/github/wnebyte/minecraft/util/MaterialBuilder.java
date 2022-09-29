package com.github.wnebyte.minecraft.util;

import com.github.wnebyte.minecraft.renderer.Material;
import com.github.wnebyte.minecraft.renderer.Texture;

public class MaterialBuilder {

    private Texture diffuse = null;

    private Texture specular = null;

    private float shininess = 0.0f;

    private String diffusePath = null;

    private String specularPath = null;

    public MaterialBuilder setDiffuse(Texture diffuse) {
        this.diffuse = diffuse;
        this.specular = diffuse;
        return this;
    }

    public MaterialBuilder setSpecular(Texture specular) {
        this.specular = specular;
        return this;
    }

    public MaterialBuilder setShininess(float shininess) {
        this.shininess = shininess;
        return this;
    }

    public MaterialBuilder setDiffusePath(String diffusePath) {
        this.diffusePath = diffusePath;
        return this;
    }

    public MaterialBuilder setSpecularPath(String specularPath) {
        this.specularPath = specularPath;
        return this;
    }

    public Material build() {
        if (diffusePath != null) {
            setDiffuse(Assets.getTexture(diffusePath));
        }
        if (specularPath != null) {
            setSpecular(Assets.getTexture(specularPath));
        }
        return new Material(diffuse, specular, shininess);
    }
}
