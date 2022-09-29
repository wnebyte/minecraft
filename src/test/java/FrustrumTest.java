import org.junit.Test;
import org.junit.Assert;
import static java.lang.System.out;

public class FrustrumTest {

    public enum Plane {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP,
        NEAR,
        FAR;
    }

    public static final int COUNT = Plane.values().length;

    public static final int COMBINATIONS = COUNT * (COUNT - 1) / 2;

    public static int ij2k(Plane i, Plane j) {
        int k = i.ordinal() * (9 - i.ordinal()) / 2 + j.ordinal() - 1;
        return k;
    }

    public static int[] intersection(Plane a, Plane b, Plane c) {
        int k1 = ij2k(b, c);
        System.out.printf("ij2k<b, c>: %d", k1);
        int k2 = ij2k(a, c);
        System.out.printf(", ij2k<a, c>: %d", k2);
        int k3 = ij2k(a, b);
        System.out.printf(", ij2k<a, b>: %d\n", k3);
        return new int[]{k1,k2,k3};
    }

    @Test
    public void test00() {
        out.print("<Plane::Left,  Plane::Bottom, Plane::Near>: ");
        Assert.assertArrayEquals(new int[]{10,3,1}, intersection(Plane.LEFT, Plane.BOTTOM, Plane.NEAR));

        out.print("<Plane::Left,  Plane::Top,    Plane::Near>: ");
        Assert.assertArrayEquals(new int[]{12,3,2}, intersection(Plane.LEFT, Plane.TOP, Plane.NEAR));

        out.print("<Plane::Right, Plane::Bottom, Plane::Near>: ");
        Assert.assertArrayEquals(new int[]{10,7,5}, intersection(Plane.RIGHT, Plane.BOTTOM, Plane.NEAR));

        out.print("<Plane::Right, Plane::Top,    Plane::Near>: ");
        Assert.assertArrayEquals(new int[]{12,7,6}, intersection(Plane.RIGHT, Plane.TOP, Plane.NEAR));

        out.print("<Plane::Left,  Plane::Bottom, Plane::Far>:  ");
        Assert.assertArrayEquals(new int[]{11,4,1}, intersection(Plane.LEFT, Plane.BOTTOM, Plane.FAR));

        out.print("<Plane::Left,  Plane::Top,    Plane::Far>:  ");
        Assert.assertArrayEquals(new int[]{13,4,2}, intersection(Plane.LEFT, Plane.TOP, Plane.FAR));

        out.print("<Plane::Right, Plane::Bottom, Plane::Far>:  ");
        Assert.assertArrayEquals(new int[]{11,8,5}, intersection(Plane.RIGHT, Plane.BOTTOM, Plane.FAR));

        out.print("<Plane::Right, Plane::Top,    Plane::Far>:  ");
        Assert.assertArrayEquals(new int[]{13,8,6}, intersection(Plane.RIGHT, Plane.TOP, Plane.FAR));
    }
}
