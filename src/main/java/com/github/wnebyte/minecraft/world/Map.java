package com.github.wnebyte.minecraft.world;

import java.util.HashMap;
import org.joml.Vector2i;

public class Map {

    public java.util.Map<Vector2i, Chunk> chunks;

    public Map() {
        this(10);
    }

    public Map(int initialCapacity) {
        this.chunks = new HashMap<>();
    }

    public Chunk put(Vector2i key, Chunk value) {
        return chunks.put(key, value);
    }

    public Chunk get(int x, int z) {
        Vector2i key = new Vector2i(x, z);
        return chunks.get(key);
    }
}
