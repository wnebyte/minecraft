package com.github.wnebyte.minecraft.world;

import org.joml.Vector2i;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;

public class Subchunk {

    public VertexBuffer data;

    public int first;

    public int drawCommandIndex;

    public Vector2i chunkCoords;

    public boolean isBlendable;

    public Subchunk(VertexBuffer data) {
        this.data = data;
    }

    public VertexBuffer getData() {
        return data;
    }

    public int getFirst() {
        return first;
    }

    public int getDrawCommandIndex() {
        return drawCommandIndex;
    }

    public Vector2i getChunkCoords() {
        return chunkCoords;
    }

    public boolean isBlendable() {
        return isBlendable;
    }
}
