package com.github.wnebyte.minecraft.util;

import java.nio.IntBuffer;
import java.util.Comparator;

public class BufferUtils {

    public static int binarySearch(IntBuffer buffer, int key) {
        return binarySearch(buffer, key, Integer::compare);
    }

    public static int binarySearch(IntBuffer buffer, int fromIndex, int toIndex, int key) {
        return binarySearch(buffer, fromIndex, toIndex, key, Integer::compare);
    }

    public static int binarySearch(IntBuffer buffer, int key, Comparator<? super Integer> c) {
        return binarySearch(buffer, 0, buffer.capacity(), key, c);
    }

    public static int binarySearch(IntBuffer buffer, int fromIndex, int toIndex, int key, Comparator<? super Integer> c) {
        rangeCheck(buffer.capacity(), fromIndex, toIndex);
        int mid = (fromIndex + toIndex) / 2;

        while (fromIndex <= toIndex) {
            if (c.compare(buffer.get(mid), key) == 0) {
                return mid;
            }
            else if (c.compare(buffer.get(mid), key) < 0) {
                fromIndex = mid + 1;
            }
            else {
                toIndex = mid - 1;
            }
            mid = (fromIndex + toIndex) / 2;
        }

        return -1;
    }

    public static boolean rangeCheck(int capacity, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("");
        }
        if (fromIndex < 0 || toIndex > capacity) {
            throw new IndexOutOfBoundsException("");
        }
        return true;
    }
}
