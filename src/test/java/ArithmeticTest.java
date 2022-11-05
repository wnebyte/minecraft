import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class ArithmeticTest {

    private static final int BASE_17_WIDTH = 17;

    private static final int BASE_17_HEIGHT = 17;

    private static final int BASE_17_DEPTH = 289;

    private static final int WIDTH = 16, HEIGHT = 16, DEPTH = 16;

    private int[][][] array;

    private Random rand = new Random();

    @Before
    public void setup() {
        array = new int[WIDTH][HEIGHT][DEPTH];
        int index = 0;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    array[x][y][z] = index++;
                }
            }
        }
    }

    @Test
    public void test00() {
        for (int i = 0; i < 16 * 16 * 16; i++) {
            Vector3f coords = toCoordinates(i);
            System.out.printf("x: %.2f, y: %.2f, z: %.2f\n", coords.x, coords.y, coords.z);
        }
    }

    @Test
    public void testBitMask00() {
        int a = 0;
        int b = 5;
        int c = 10;

        int aMask = 0xF00;
        int bMask = 0xF0;
        int cMask = 0xF;

        int x = (a << 8) | (b << 4) | c;
        int aNumber = (x & aMask) >> 8;
        int bNumber = (x & bMask) >> 4;
        int cNumber = (x & cMask);

        System.out.println("x: " + x);
        System.out.println("aNumber: " + aNumber);
        System.out.println("bNumber: " + bNumber);
        System.out.println("cNumber: " + cNumber);
    }

    // n = 2^10 - 1 = 1023
    @Test
    public void testBitMask01() {
        int a = 22;
        int b = 575;
        int c = 120;

        // 0011 1111 1111 0000 0000 0000 0000 0000
        int aMask = 0x3FF00000;
        // 0000 0000 0000 1111 1111 1100 0000 0000
        int bMask = 0xFFC00;
        // 0000 0000 0000 0000 0000 0011 1111 1111
        int cMask = 0x3FF;

        int x = (a << 20) | (b << 10) | c;
        int aNumber = (x & aMask) >> 20;
        int bNumber = (x & bMask) >> 10;
        int cNumber = (x & cMask);

        System.out.println("x: " + x);
        System.out.println("aNumber: " + aNumber);
        System.out.println("bNumber: " + bNumber);
        System.out.println("cNumber: " + cNumber);
    }

    @Test
    public void bitMaskTest02() {
        int x, y, z; // range(0, 31)
        byte texId;
        byte face; // range(0, 6)
    }

    @Test
    public void toIndexTest00() {
        int index = toIndex(0, 0, 0);
        int compare = 0;
        Assert.assertEquals(compare, index);

        /*
        int x = rand(), y = rand(), z = rand();
        index = toIndex(x, y, z);
        compare = array[x][y][z];
        Assert.assertEquals(compare, index);
         */
    }

    @Test
    public void bitShiftTest00() {
        System.out.println(1 << 6);
    }

    @Test
    public void test01() {
        long val = (long)10E2;
        Assert.assertEquals(1000L, val);
        val = (long)1E2;
        Assert.assertEquals(100L, val);
    }

    // 0, 1, 2, 3, 4, 5, 6, 7
    // 0, 0, 1, 1, 2, 2, 3, 3
    @Test
    public void test02() {
        int size = 10;
        for (int i = 0; i < size; i++) {
            int index = i / 2;
            System.out.println(index);
        }
    }

    @Test
    public void test03() {
        Vector4f zeroFillerVec = new Vector4f(0.0f);
        Vector4f oneFillerVec = new Vector4f(1.0f);
        System.out.printf("x: %.0f, y: %.0f, z: %.0f, w: %.0f%n", zeroFillerVec.x, zeroFillerVec.y, zeroFillerVec.z, zeroFillerVec.w);
        System.out.printf("x: %.0f, y: %.0f, z: %.0f, w: %.0f%n", oneFillerVec.x, oneFillerVec.y, oneFillerVec.z, oneFillerVec.w);
    }

    @Test
    public void test04() {
        int iMax = 16, jMax = 256, kMax = 16;

        // YN(-)
        System.out.println("YN(-)");
        for (int j = 0; j < jMax; j++) {
            int subchunkLevel = j / 16;
            if (j % 16 == 0 && subchunkLevel != 0) {
                System.out.print(j + ", ");
            }
        }
        // YP(+)
        System.out.println("\nYP(+)");
        for (int j = 0; j < jMax; j++) {
            int subchunkLevel = j / 16;
            if ((j + 1) % 16 == 0 && subchunkLevel != 15) {
                System.out.print(j + ", ");
            }
        }
    }

    @Test
    public void test05() {
        Vector3f center     = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f dimensions = new Vector3f(1f, 1f, 1f);
        addBox3D(center, dimensions);
    }

    @Test
    public void test06() {
        for (int i = 0; i < 36; i++) {
            int mod = (i % 6);
            System.out.println(getFace(i));
        }
    }

    @Test
    public void test07() {
        int[] items = { 1, 2, 3, 4 ,5, 6, 7, 8, 9 };
        int[] bar = { 10, 11, 12, 13, 14, 15, 16, 17, 18 };
        int index = 10;
        if (index >= items.length) {
            index = (index - items.length);
            int i = bar[index];
            System.out.println(i);
        } else {
            int i = items[index];
            System.out.println(i);
        }
    }

    private String getFace(int index) {
        int mod = (index % 6);
        switch (mod) {
            case 4:
                return "TOP";
            case 5:
                return "BOTTOM";
            default:
                return "SIDE";
        }
    }

    private void addBox3D(Vector3f center, Vector3f dimensions) {
        Vector3f min = new Vector3f(center).sub(new Vector3f(dimensions).mul(0.5f));
        Vector3f max = new Vector3f(center).add(new Vector3f(dimensions).mul(0.5f));

        System.out.printf("x: %.2f, y: %.2f, z: %.2f%n", min.x, min.y, min.z);
        System.out.printf("x: %.2f, y: %.2f, z: %.2f%n", max.x, max.y, max.z);

        Vector3f[] vertices = {

        };
    }

    private Vector3f toCoordinates(int index) {
        int z = index % BASE_17_WIDTH;
        int x = (index % BASE_17_HEIGHT) / BASE_17_DEPTH;
        int y = (index - (x * BASE_17_DEPTH) - z) / BASE_17_HEIGHT;
        return new Vector3f(x, y, z);
    }

    public int toIndex(int x, int y, int z) {
        return (z * WIDTH * HEIGHT) + (y * HEIGHT) + x;
    }

    public int rand() {
        return rand.nextInt(WIDTH);
    }
}
