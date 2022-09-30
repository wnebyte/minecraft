package com.github.wnebyte.minecraft.util;

import java.util.*;

public class Pool<K, V> implements Iterable<V> {

    private final Map<K, V> pool;

    private final Queue<V> queue;

    private int size;

    public Pool(int size) {
        this.pool = new HashMap<>(size);
        this.queue = new LinkedList<>();
        this.size = size;
    }

    public void add(V v) {
        queue.add(v);
    }

    public V get(K k) {
        if (pool.containsKey(k)) {
            return pool.get(k);
        } else if (hasRoom()) {
            V v = queue.poll();
            pool.put(k, v);
            return v;
        }
        return null;
    }

    public boolean free(K k) {
        if (pool.containsKey(k)) {
            V v = pool.remove(k);
            queue.add(v);
            return true;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean hasRoom() {
        return (pool.size() < size && queue.size() > 0);
    }

    @Override
    public Iterator<V> iterator() {
        return pool.values().iterator();
    }
}
