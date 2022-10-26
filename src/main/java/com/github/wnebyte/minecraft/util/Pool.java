package com.github.wnebyte.minecraft.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Pool<K, V> implements Iterable<V> {

    private final Map<K, V> pool;

    private final Queue<V> queue;

    private int capacity;

    public Pool(int capacity) {
        this.pool = new ConcurrentHashMap<>(capacity);
        this.queue = new ConcurrentLinkedQueue<>();
        this.capacity = capacity;
    }

    public void add(V v) {
        queue.add(v);
    }

    public V get(K k) {
        if (pool.containsKey(k)) {
            return pool.get(k);
        } else {
            V v = queue.poll();
            if (v != null) {
                pool.put(k, v);
                return v;
            }
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

    @Override
    public Iterator<V> iterator() {
        return pool.values().iterator();
    }
}
