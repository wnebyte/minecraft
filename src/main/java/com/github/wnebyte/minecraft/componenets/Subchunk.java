package com.github.wnebyte.minecraft.componenets;

import org.joml.Vector2i;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;

public class Subchunk {

    public VertexBuffer data;

    public int firstIndex;

    public int drawCommandIndex;

    public int numVertsUsed;

    public Vector2i chunkCoords;

    public short subchunkLevel;
}
