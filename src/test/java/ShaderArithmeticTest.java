import org.joml.Vector3f;
import org.joml.Vector3i;
import com.github.wnebyte.minecraft.world.Chunk;
import org.junit.Test;

public class ShaderArithmeticTest {

    // 1111 1111 1111 1111 0000 0000 0000 0000
    private static final int POSITION_BITMASK = 0xFFFF0000;

    // 0000 0000 0000 0000 1111 1111 1100 0000
    private static final int UV_BITMASK = 0xFFC0;

    // 0000 0000 0000 0000 0000 0000 0011 1000
    private static final int FACE_BITMASK = 0x38;

    // 0000 0000 0000 0000 0000 0000 0000 0111
    private static final int VERTEX_BITMASK = 0x7;

    public static Vector3i extractPosition(int index) {
        //int index = (data & POSITION_BITMASK) >> 16;
        int z = index % Chunk.WIDTH;
        int x = (index % Chunk.HEIGHT) / Chunk.DEPTH;
        int y = (index - (x * Chunk.DEPTH) - z) / Chunk.HEIGHT;
        Vector3i pos = new Vector3i(x, y, z);
        return pos;
    }

    public static Vector3i toIndex3D(int index) {
        int z = index / (Chunk.WIDTH * Chunk.HEIGHT);
        index -= (z * Chunk.WIDTH * Chunk.HEIGHT);
        int y = index / Chunk.WIDTH;
        int x = index % Chunk.WIDTH;
        return new Vector3i(x, y, z);
    }

    public static int toIndex(int x, int y, int z) {
        return (z * Chunk.WIDTH * Chunk.HEIGHT) + (y * Chunk.WIDTH) + x;
    }

    @Test
    public void test00() {
        int index = toIndex(15, 80, 3);
        Vector3i i1 = toIndex3D(index);
        Vector3i i2 = extractPosition(index);
        System.out.printf("index:           %d%n", index);
        System.out.printf("toIndex3D:       x: %d, y: %d, z: %d%n", i1.x, i1.y, i1.z);
        System.out.printf("extractPosition: x: %d, y: %d, z: %d%n", i2.x, i2.y, i2.z);
    }

}
