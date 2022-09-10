package com.github.wnebyte.minecraft.util;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.light.Light;

public class LightBuilder {

    private Vector3f ambient;

    private Vector3f diffuse;

    private Vector3f specular;

    public LightBuilder setAmbient(Vector3f ambient) {
        this.ambient = ambient;
        return this;
    }

    public LightBuilder setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
        return this;
    }

    public LightBuilder setSpecular(Vector3f specular) {
        this.specular = specular;
        return this;
    }

    public Light build() {
        return new Light(ambient, diffuse, specular);
    }
}
