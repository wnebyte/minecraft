package com.github.wnebyte.minecraft.components;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.Cube3D;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Volatile;

public class Particle3D extends Cube3D implements Volatile {

    public Vector3f velocity;

    private int ftl;

    public Particle3D(
            Transform transform,
            Vector3f color,
            Sprite sideSprite,
            Sprite topSprite,
            Sprite bottomSprite,
            Vector3f velocity,
            int ftl) {
        super(transform, color, sideSprite, topSprite, bottomSprite);
        this.velocity = velocity;
        this.ftl = ftl;
    }

    @Override
    public int ftl() {
        return ftl--;
    }
}
