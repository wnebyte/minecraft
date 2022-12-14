package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class Slice {

    private int fromIndex;

    private int toIndex;

    public Slice() {
        this(0, 0);
    }

    public Slice(int fromIndex) {
        this(fromIndex, 0);
    }

    public Slice(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public void addFromIndex(int value) {
        this.fromIndex += value;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public void addToIndex(int value) {
        this.toIndex += value;
    }

    public int size() {
        return toIndex - fromIndex;
    }

    public void set(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Slice)) return false;
        Slice slice = (Slice)o;
        return Objects.equals(slice.fromIndex, this.fromIndex) &&
                Objects.equals(slice.toIndex, this.toIndex);
    }

    @Override
    public int hashCode() {
        int result = 3;
        return 2 *
                result +
                Objects.hashCode(this.fromIndex) +
                Objects.hashCode(this.toIndex);
    }

    @Override
    public String toString() {
        return String.format("Slice[fromIndex: %d, toIndex: %d]", this.fromIndex, this.toIndex);
    }
}
