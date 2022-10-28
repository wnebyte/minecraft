package com.github.wnebyte.minecraft.util;

import java.nio.IntBuffer;

public class BufferSort {

    /**
     * Sorts a range of the specified {@link IntBuffer} in ascending order (lowest
     * first). The sort is:
     * <ul>
     * <li>in-place
     * <li>{@code O(n*log(n))} in the worst case
     * <li>a good general-purpose sorting algorithm
     * </ul>
     *
     * @param b         the buffer to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     *
     * @throws IllegalArgumentException  if {@code fromIndex > toIndex}
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0 or toIndex > b.capacity()}
     */
    public static void heapSort(IntBuffer b, int fromIndex, int toIndex) {
        rangeCheck(b.capacity(), fromIndex, toIndex);

        int n = toIndex - fromIndex;
        if (n <= 1)
            return;

        // Build max heap
        for (int i = fromIndex + n / 2 - 1; i >= fromIndex; i--)
            heapify(b, toIndex, i, fromIndex);

        // Heap sort
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            swap(b, fromIndex, i);

            // Heapify root element
            heapify(b, i, fromIndex, fromIndex);
        }
    }

    // based on https://www.programiz.com/dsa/heap-sort
    private static void heapify(IntBuffer b, int n, int i, int offset) {
        // Find largest among root, left child and right child
        int largest = i;
        int l = 2 * i + 1 - offset;
        int r = l + 1;

        if (l < n && b.get(l) > b.get(largest))
            largest = l;

        if (r < n && b.get(r) > b.get(largest))
            largest = r;

        // Swap and continue heapifying if root is not largest
        if (largest != i) {
            swap(b, i, largest);
            heapify(b, n, largest, offset);
        }
    }

    private static void swap(IntBuffer b, int i, int j) {
        int swap = b.get(i);
        b.put(i, b.get(j));
        b.put(j, swap);
    }

    /**
     * Sorts a range of the specified {@link IntBuffer} in ascending order (lowest
     * first). The sort is:
     * <ul>
     * <li>in-place
     * <li>{@code O(n)} in the worst case. However, insertion sort has more overhead
     * than heat sort, and is only faster for large ranges.
     * </ul>
     *
     * @param b         the buffer to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     *
     * @throws IllegalArgumentException  if {@code fromIndex > toIndex}
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0 or toIndex > b.capacity()}
     */
    public static void radixSort(IntBuffer b, int fromIndex, int toIndex) {
        rangeCheck(b.capacity(), fromIndex, toIndex);
        radixSort0(b, fromIndex, toIndex, INT_HIGH_BIT);
    }

    private static final int INT_HIGH_BIT = 1 << 31;

    private static void radixSort0(IntBuffer b, int fromIndex, int toIndex, int bit) {
        int zero = fromIndex;
        int one = toIndex;

        final int direction = bit == INT_HIGH_BIT ? bit : 0;

        while (zero < one) {
            if ((b.get(zero) & bit) == direction)
                zero++;
            else
                swap(b, zero, --one);
        }

        if (bit != 1) {
            if (fromIndex < zero)
                radixSort0(b, fromIndex, zero, bit >>> 1);
            if (one < toIndex)
                radixSort0(b, one, toIndex, bit >>> 1);
        }
    }

    /**
     * Sorts a range of the specified {@link IntBuffer} in ascending order (lowest
     * first). The sort is:
     * <ul>
     * <li>in-place
     * <li>{@code O(n^2)} in the worst case. However, insertion sort has less
     * overhead than heat sort, and is faster for small ranges.
     * </ul>
     *
     * @param b         the buffer to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     *
     * @throws IllegalArgumentException  if {@code fromIndex > toIndex}
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0 or toIndex > b.capacity()}
     */
    public static void insertionSort(IntBuffer b, int fromIndex, int toIndex) {
        rangeCheck(b.capacity(), fromIndex, toIndex);

        for (int i = fromIndex + 1; i < toIndex; i++) {
            int x = b.get(i);
            int j = i - 1;

            for (int xj; j >= fromIndex && (xj = b.get(j)) > x; j--)
                b.put(j + 1, xj);
            b.put(j + 1, x);
        }
    }

    private static final int SMALL_RANGE = 100;
    private static final int LARGE_RANGE = 10_000_000;

    /**
     * Sorts a range of the specified {@link IntBuffer} in ascending order (lowest
     * first). The actual sorting algorithm used depends on the length of the range:
     * <table border=1 summary="Sorting algorithm by array length">
     * <tr>
     * <th>Length</th>
     * <th>Algorithm</th>
     * </tr>
     * <tr>
     * <td>{@code [0 - 100)}</td>
     * <td>{@link BufferSort#insertionSort(IntBuffer, int, int) insertionSort}</td>
     * </tr>
     * <tr>
     * <td>{@code [100 - 10^7)}</td>
     * <td>{@link BufferSort#heapSort(IntBuffer, int, int) heapSort}</td>
     * </tr>
     * <tr>
     * <td>{@code 10^7+}</td>
     * <td>{@link BufferSort#radixSort(IntBuffer, int, int) radixSort}</td>
     * </tr>
     * </table>
     *
     * @param b         the buffer to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     *
     * @throws IllegalArgumentException  if {@code fromIndex > toIndex}
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0 or toIndex > b.capacity()}
     */
    public static void sort(IntBuffer b, int fromIndex, int toIndex) {

        final int length = toIndex - fromIndex;

        if (length < SMALL_RANGE)
            insertionSort(b, fromIndex, toIndex);
        else if (length < LARGE_RANGE)
            heapSort(b, fromIndex, toIndex);
        else
            radixSort(b, fromIndex, toIndex);
    }

    public static boolean rangeCheck(int capacity, int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("");
        if (fromIndex < 0 || toIndex > capacity)
            throw new IndexOutOfBoundsException("");
        return true;
    }
}
