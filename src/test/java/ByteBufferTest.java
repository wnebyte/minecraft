import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteBufferTest {

    @Test
    public void test00() {
        int size = 12;
        ByteBuffer src = allocate(12);
        List<ByteBuffer> buffers = slice(src, 12);
        ByteBuffer buffer = buffers.get(buffers.size() - 1);
        buffer.rewind();
        System.out.println(toString(buffer));
        buffer.put(0, (byte)99);
        System.out.println(toString(src));
        System.out.println(toString(buffer));
    }

    private static List<ByteBuffer> slice(ByteBuffer src, int size) {
        int numBuffers = 4;
        int length = size / numBuffers; // 3
        List<ByteBuffer> buffers = new ArrayList<>(numBuffers);

        for (int offset = 0; offset <= size - length; offset += length) {
            src.position(offset);
            src.limit(offset + length);
            ByteBuffer dest = src.slice();
            dest.rewind();
            buffers.add(dest);
        }

        return buffers;
    }

    private static ByteBuffer allocate(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        for (int i = 0; i < Math.min(capacity, Byte.MAX_VALUE + 1); i++) {
            buffer.put((byte)i);
        }
        return buffer;
    }

    private static ByteBuffer allocate() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.put(new byte[]{ 0,1,2, 3,4,5, 6,7,8, 9,10,11 });
        buffer.rewind();
        return buffer;
    }

    private static String toString(ByteBuffer buffer) {
        StringBuilder s = new StringBuilder();
        buffer.rewind();
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            s.append(b);
            s.append(", ");
        }
        buffer.rewind();
        return s.toString();
    }
}
