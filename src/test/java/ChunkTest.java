import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.world.Chunk;
import org.joml.Vector3i;
import org.junit.Assert;
import org.junit.Test;

public class ChunkTest {

    private static final int
            WIDTH = 16, HEIGHT = 16, DEPTH = 16,
            CHUNK_WIDTH = WIDTH, CHUNK_HEIGHT = HEIGHT, CHUNK_DEPTH = DEPTH,
            CHUNK_SIZE = CHUNK_WIDTH,
            CHUNK_SIZE_SQUARED = (CHUNK_SIZE * CHUNK_SIZE),
            CHUNK_SIZE_SHIFTED = 16 << 6;

    public ChunkTest() {
        Block[] data = new Block[CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_DEPTH];
    }

    public int toIndex(int x, int y, int z) {
        return (z * CHUNK_SIZE * CHUNK_SIZE) + (y * CHUNK_SIZE) + x;
    }

    public int toIndexAlt(int i, int j, int k) {
        return i + j * CHUNK_SIZE + k * (CHUNK_SIZE * CHUNK_SIZE);
    }

    public static Vector3i to3D(int index) {
        int z = index / (CHUNK_SIZE * CHUNK_SIZE);
        index -= (z * CHUNK_SIZE * CHUNK_SIZE);
        int y = index / CHUNK_SIZE;
        int x = index % CHUNK_SIZE;
        return new Vector3i(x, y, z);
    }

    @Test
    public void test01() {
        for (int y = 0; y < CHUNK_DEPTH; y++) {
            for (int z = 0; z < CHUNK_HEIGHT; z++) {
                for (int x = 0; x < CHUNK_WIDTH; x++) {
                    int index = toIndex(x, y, z);
                    int compare = Chunk.toIndex(x, y, z);
                    Vector3i v = to3D(index);
                    Assert.assertEquals(compare, index);
                    Assert.assertEquals(x, v.x);
                    Assert.assertEquals(y, v.y);
                    Assert.assertEquals(z, v.z);
                }
            }
        }
    }

    @Test
    public void test02() {
        int j = 6;
        int j1 = j + 1;
        int jS  = j << 6;
        int jS1 = j1 << 6;
        int length;

        for (length = jS1; length < CHUNK_SIZE_SHIFTED; length += (1 << 6)) {
            System.out.println(length);
        }
    }

    @Test
    public void test03() {
        int i = 5;
        int j = 10;
        int k = 12;
        int index = toIndex(31, j, k);
        int compare = 31 + j * CHUNK_SIZE + k * CHUNK_SIZE_SQUARED;
        Assert.assertEquals(compare, index);
    }

    @Test
    public void test04() {
        System.out.println(32 * 32);
    }
}
