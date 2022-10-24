package com.github.wnebyte.minecraft.util;

import java.util.*;

public class Pool<K, V> implements Iterable<V> {

    private final Map<K, V> pool;

    private final Queue<V> queue;

    private int capacity;

    public Pool(int capacity) {
        this.pool = new HashMap<>(capacity);
        this.queue = new LinkedList<>();
        this.capacity = capacity;
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

    public int capacity() {
        return capacity;
    }

    public int size() {
        return pool.size();
    }

    public int remaining() {
        return queue.size();
    }

    public boolean hasRoom() {
        return (size() < capacity() && remaining() > 0);
    }

    @Override
    public Iterator<V> iterator() {
        return pool.values().iterator();
    }
}
