import org.joml.Vector3f;
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
