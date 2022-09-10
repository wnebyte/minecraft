package com.github.wnebyte.minecraft.util;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.Block;
import com.github.wnebyte.minecraft.renderer.Material;

public class BlockBuilder {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);

    private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    private float rotation = 0.0f;

    private String texturePath = null;

    private Material material = null;

    public BlockBuilder() {}

    public BlockBuilder setPosition(Vector3f position) {
        this.position = position;
        return this;
    }

    public BlockBuilder setScale(Vector3f scale) {
        this.scale = scale;
        return this;
    }

    public BlockBuilder setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public BlockBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public Block build() {
        return new Block(new Transform(position, scale, rotation), material);
    }
}
