package com.github.wnebyte.minecraft.util;

import com.github.wnebyte.minecraft.componenets.BoxRenderer;
import com.github.wnebyte.minecraft.renderer.Texture;
import org.joml.Vector3f;

public class BlockBuilder {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);

    private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    private float rotation = 0.0f;

    private Texture side, top, bottom;

    private String sidePath, topPath, bottomPath;

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

    public BlockBuilder setSideTexture(Texture side) {
        this.side = side;
        this.top = side;
        this.bottom = side;
        return this;
    }

    public BlockBuilder setTopTexture(Texture top) {
        this.top = top;
        return this;
    }

    public BlockBuilder setBottomTexture(Texture bottom) {
        this.bottom = bottom;
        return this;
    }

    public BoxRenderer build() {
        if (sidePath != null) {
            side = Assets.getTexture(sidePath);
        }
        if (topPath != null) {
            top = Assets.getTexture(topPath);
        }
        if (bottomPath != null) {
            bottom = Assets.getTexture(bottomPath);
        }
        //return new BoxRenderer(new Transform(position, scale, rotation), side, top, bottom);
        return null;
    }
}
