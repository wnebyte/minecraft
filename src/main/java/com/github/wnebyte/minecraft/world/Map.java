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

    public Chunk putChunk(Vector2i key, Chunk value) {
        return chunks.put(key, value);
    }

    public Chunk putChunk(Chunk value) {
        return putChunk(value.getChunkCoords(), value);
    }

    public Chunk removeChunk(Vector2i key) {
        return chunks.remove(key);
    }

    public Chunk removeChunk(Chunk value) {
        return removeChunk(value.getChunkCoords());
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

    public Set<Chunk> getChunksBeyondRadius(Vector2i v, int radius) {
        Set<Chunk> chunks = new HashSet<>();
        for (Chunk chunk : this) {
            Vector2i chunkCoords = chunk.getChunkCoords();
            int dx = Math.abs(v.x - chunkCoords.x);
            int dy = Math.abs(v.y - chunkCoords.y);
            int ds = (dx + dy);
            if (ds > radius) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public Set<Vector2i> getChunkCoordsWithinRadius(Vector2i v, int radius) {
        Set<Vector2i> set = new HashSet<>();
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                Vector2i ivec2 = new Vector2i(v.x + i, v.y + j);
                int dx = Math.abs(v.x - ivec2.x);
                int dy = Math.abs(v.y - ivec2.y);
                int ds = (dx + dy);
                if (ds < radius) {
                    set.add(ivec2);
                }
            }
        }
        return set;
    }

    public boolean contains(Vector2i v) {
        return chunks.containsKey(v);
    }

    public int size() {
        return chunks.size();
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
