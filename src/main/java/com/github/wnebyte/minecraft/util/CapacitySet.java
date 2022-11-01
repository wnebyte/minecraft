package com.github.wnebyte.minecraft.util;

import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedHashSet;

public class CapacitySet<E> implements Set<E> {

    private final Set<E> data;

    private final int capacity;

    public CapacitySet(int capacity) {
        this.capacity = capacity;
        this.data = new LinkedHashSet<>(capacity);
    }

    public int indexOf(E e) {
        int index = 0;
        for (E element : data) {
            if (element.equals(e)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public boolean add(E e) {
        if (size() < capacity) {
            data.add(e);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        return data.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean mod = false;
        for (E e : c) {
            boolean success = add(e);
            if (success) {
                mod = true;
            }
        }
        return mod;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return data.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return data.removeAll(c);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }
}
