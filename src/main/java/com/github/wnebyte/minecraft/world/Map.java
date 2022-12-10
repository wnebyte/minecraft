package com.github.wnebyte.minecraft.world;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Map implements Iterable<Chunk> {

    private final java.util.Map<Vector2i, Chunk> chunks;

    public Map() {
        this(10);
    }

    public Map(int initialCapacity) {
        this.chunks = new ConcurrentHashMap<>(initialCapacity);
    }

    public Chunk put(Vector2i key, Chunk value) {
        return chunks.put(key, value);
    }

    public Chunk put(Chunk value) {
        return put(value.getChunkCoords(), value);
    }

    public Chunk remove(Vector2i key) {
        return chunks.remove(key);
    }

    public Chunk remove(Chunk value) {
        return remove(value.getChunkCoords());
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

    public Chunk getChunk(Vector3f pos) {
        return getChunk(pos.x, pos.y, pos.z);
    }

    public Block getBlock(Vector3f pos) {
        int i = (int)Math.floor(pos.x / Chunk.WIDTH);
        int j = (int)Math.floor(pos.y);
        int k = (int)Math.floor(pos.z / Chunk.DEPTH);
        Chunk chunk = getChunk(i, k);
        if (chunk != null && j >= 0 && j < Chunk.HEIGHT) {
            i = (int)Math.floor(pos.x - (i * Chunk.WIDTH));
            k = (int)Math.floor(pos.z - (k * Chunk.DEPTH));
            return chunk.getBlock(i, j, k);
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

    public boolean contains(Vector2i key) {
        return chunks.containsKey(key);
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
