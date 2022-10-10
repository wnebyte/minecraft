package com.github.wnebyte.minecraft.world;

import java.util.*;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Map implements Iterable<Chunk> {

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

    public Chunk getChunk(int i, int k) {
        Vector2i key = new Vector2i(i, k);
        return chunks.get(key);
    }

    public Chunk getChunk(float x, float y, float z) {
        int i = (int)Math.floor(x / Chunk.WIDTH);
        int k = (int)Math.floor(z / Chunk.DEPTH);
        return getChunk(i, k);
    }

    public Block getBlock(Vector3f v) {
        int i = (int)Math.floor(v.x / Chunk.WIDTH);
        int j = (int)Math.floor(v.y);
        int k = (int)Math.floor(v.z / Chunk.DEPTH);
        Chunk chunk = getChunk(i, k);
        if (chunk != null && j >= 0 && j < Chunk.HEIGHT - 1) {
            i = (int)Math.floor(v.x) - (i * Chunk.WIDTH);
            k = (int)Math.floor(v.z) - (k * Chunk.DEPTH);
            Block b = chunk.getBlock(i, j, k);
            return b;
        }
        return null;
    }

    public Set<Vector2i> keys() {
        return Collections.unmodifiableSet(chunks.keySet());
    }

    public Collection<Chunk> values() {
        return Collections.unmodifiableCollection(chunks.values());
    }

    @Override
    public Iterator<Chunk> iterator() {
        return chunks.values().iterator();
    }
}
