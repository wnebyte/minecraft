import org.junit.Assert;
import org.junit.Test;

// position index - 16 bits
// uv index       - 10 bits
// face index     - 3 bits
// vertex index   - 3 bits
public class BitshiftTest {

    // 1111 1111 1111 1111 0000 0000 0000 0000
    private static final int POSITION_BITMASK = 0xFFFF0000;

    // 0000 0000 0000 0000 1111 1111 1100 0000
    private static final int UV_BITMASK = 0xFFC0;

    // 0000 0000 0000 0000 0000 0000 0011 1000
    private static final int FACE_BITMASK = 0x38;

    // 0000 0000 0000 0000 0000 0000 0000 0111
    private static final int VERTEX_BITMASK = 0x7;

    @Test
    public void test00() {
        long position = (16 - 1) * (256 - 1) * (16 - 1);
        int uv = 650;
        int face = 0;
        int vertex = 3;

        long data = (position << 16) | (uv << 6) | (face << 3) | vertex;
        long aPosition = (data & POSITION_BITMASK) >> 16;
        int aUv       = (int)(data & UV_BITMASK) >> 6;
        int aFace     = (int)(data & FACE_BITMASK) >> 3;
        int aVertex   = (int)(data & VERTEX_BITMASK);

        Assert.assertEquals(position, aPosition);
        Assert.assertEquals(uv, aUv);
        Assert.assertEquals(face, aFace);
        Assert.assertEquals(vertex, aVertex);
    }

    @Test
    public void test01() {
        Integer position = (16 - 1) * (256 - 1) * (16 - 1);
    }
}
