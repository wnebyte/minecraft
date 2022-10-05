package util;

import org.junit.Test;
import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.Random;
import com.github.wnebyte.minecraft.util.BufferSort;

public class BufferSortTest {

    private static final Comparator<Integer> COMPARATOR = Integer::compare;

    private static final Random rand = new Random();

    @Test
    public void test00() {
        int size = 35;
        IntBuffer data = genBuffer(size);
        printBuffer(data);
        BufferSort.heapSort(data, 0, size, COMPARATOR);
        printBuffer(data);
    }

    private IntBuffer genBuffer(int size) {
        IntBuffer buffer = IntBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            int val = rand.nextInt(100);
            buffer.put(val);
        }
        buffer.rewind();
        return buffer;
    }

    private void printBuffer(IntBuffer buffer) {
        buffer.rewind();
        if (buffer.capacity() >=  1) {
            System.out.print("[");
        }
        for (int i = 0; i < buffer.capacity(); i++) {
            int val = buffer.get();
            System.out.print(val);
            if (i < buffer.capacity() - 1) {
                System.out.print(", ");
            }
        }
        if (buffer.capacity() >= 1) {
            System.out.println("]");
        }
        buffer.rewind();
    }
}
