package com.github.wnebyte.minecraft.world;

import java.util.*;
import org.joml.Vector2i;

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

    public Chunk get(int x, int z) {
        Vector2i key = new Vector2i(x, z);
        return chunks.get(key);
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
