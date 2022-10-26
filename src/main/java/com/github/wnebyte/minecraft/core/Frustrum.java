package com.github.wnebyte.minecraft.core;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.util.JMath;

public class Frustrum {

    private enum Plane {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP,
        NEAR,
        FAR,
    }

    private static final int COUNT = Plane.values().length;

    private static final int COMBINATIONS = COUNT * (COUNT - 1) / 2;

    private static int ij2k(Plane i, Plane j) {
        int k = i.ordinal() * (9 - i.ordinal()) / 2 + j.ordinal() - 1;
        return k;
    }

    private final Vector4f[] planes;

    private final Vector3f[] points;

    public Frustrum() {
        this.planes = new Vector4f[COUNT];
        this.points = new Vector3f[8];
    }

    private void setPlane(Plane plane, Vector4f value) {
        int index = plane.ordinal();
        planes[index] = value;
    }

    private Vector4f getPlane(Plane plane) {
        int index = plane.ordinal();
        return planes[index];
    }

    private Vector3f intersection(Plane a, Plane b, Plane c, Vector3f[] crosses) {
        Vector3f aVec = JMath.toVector3f(getPlane(a));
        Vector3f bVec = new Vector3f(crosses[ij2k(b, c)]);
        float dot = aVec.dot(bVec);
        Matrix3f mat = new Matrix3f(crosses[ij2k(b, c)], crosses[ij2k(a, c)].negate(), crosses[ij2k(a, b)]);
        Vector3f vec = new Vector3f(getPlane(a).w, getPlane(b).w, getPlane(c).w);
        Vector3f res = vec.mul(mat);
        return res.mul(-1.0f / dot);
    }

    public void update(Matrix4f mat) {
        Matrix4f transposed = new Matrix4f(mat).transpose();
        Vector4f col0 = new Vector4f();
        Vector4f col1 = new Vector4f();
        Vector4f col2 = new Vector4f();
        Vector4f col3 = new Vector4f();
        transposed.getColumn(0, col0);
        transposed.getColumn(1, col1);
        transposed.getColumn(2, col2);
        transposed.getColumn(3, col3);

        // update planes
        setPlane(Plane.LEFT,   JMath.add(col3, col0));
        setPlane(Plane.RIGHT,  JMath.sub(col3, col0));
        setPlane(Plane.BOTTOM, JMath.add(col3, col1));
        setPlane(Plane.TOP,    JMath.sub(col3, col1));
        setPlane(Plane.NEAR,   JMath.add(col3, col2));
        setPlane(Plane.FAR,    JMath.sub(col3, col2));

        Vector3f[] crosses = {
                JMath.toVector3f(getPlane(Plane.LEFT))
                        .cross(JMath.toVector3f(getPlane(Plane.RIGHT))),
                JMath.toVector3f(getPlane(Plane.LEFT))
                        .cross(JMath.toVector3f(getPlane(Plane.BOTTOM))),
                JMath.toVector3f(getPlane(Plane.LEFT))
                        .cross(JMath.toVector3f(getPlane(Plane.TOP))),
                JMath.toVector3f(getPlane(Plane.LEFT))
                        .cross(JMath.toVector3f(getPlane(Plane.NEAR))),
                JMath.toVector3f(getPlane(Plane.LEFT))
                        .cross(JMath.toVector3f(getPlane(Plane.FAR))),
                JMath.toVector3f(getPlane(Plane.RIGHT))
                        .cross(JMath.toVector3f(getPlane(Plane.BOTTOM))),
                JMath.toVector3f(getPlane(Plane.RIGHT))
                        .cross(JMath.toVector3f(getPlane(Plane.TOP))),
                JMath.toVector3f(getPlane(Plane.RIGHT))
                        .cross(JMath.toVector3f(getPlane(Plane.NEAR))),
                JMath.toVector3f(getPlane(Plane.RIGHT))
                        .cross(JMath.toVector3f(getPlane(Plane.FAR))),
                JMath.toVector3f(getPlane(Plane.BOTTOM))
                        .cross(JMath.toVector3f(getPlane(Plane.TOP))),
                JMath.toVector3f(getPlane(Plane.BOTTOM))
                        .cross(JMath.toVector3f(getPlane(Plane.NEAR))),
                JMath.toVector3f(getPlane(Plane.BOTTOM))
                        .cross(JMath.toVector3f(getPlane(Plane.FAR))),
                JMath.toVector3f(getPlane(Plane.TOP))
                        .cross(JMath.toVector3f(getPlane(Plane.NEAR))),
                JMath.toVector3f(getPlane(Plane.TOP))
                        .cross(JMath.toVector3f(getPlane(Plane.FAR))),
                JMath.toVector3f(getPlane(Plane.NEAR))
                        .cross(JMath.toVector3f(getPlane(Plane.FAR)))
        };

        // update points
        points[0] = intersection(Plane.LEFT,  Plane.BOTTOM, Plane.NEAR, crosses);
        points[1] = intersection(Plane.LEFT,  Plane.TOP,    Plane.NEAR, crosses);
        points[2] = intersection(Plane.RIGHT, Plane.BOTTOM, Plane.NEAR, crosses);
        points[3] = intersection(Plane.RIGHT, Plane.TOP,    Plane.NEAR, crosses);
        points[4] = intersection(Plane.LEFT,  Plane.BOTTOM, Plane.FAR,  crosses);
        points[5] = intersection(Plane.LEFT,  Plane.TOP,    Plane.FAR,  crosses);
        points[6] = intersection(Plane.RIGHT, Plane.BOTTOM, Plane.FAR,  crosses);
        points[7] = intersection(Plane.RIGHT, Plane.TOP,    Plane.FAR,  crosses);
    }

    // http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.html
    public boolean isBoxVisisble(Vector3f minp, Vector3f maxp) {
        // check box outside/inside of frustum
        for (int i = 0; i < COUNT; i++) {
            if ((planes[i].dot(new Vector4f(minp.x, minp.y, minp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(maxp.x, minp.y, minp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(minp.x, maxp.y, minp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(maxp.x, maxp.y, minp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(minp.x, minp.y, maxp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(maxp.x, minp.y, maxp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(minp.x, maxp.y, maxp.z, 1.0f)) < 0.0f) &&
                    (planes[i].dot(new Vector4f(maxp.x, maxp.y, maxp.z, 1.0f)) < 0.0f))
            {
                return false;
            }
        }

        // check frustum outside/inside box
        int out;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].x > maxp.x) ? 1 : 0); if (out == 8) return false;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].x < minp.x) ? 1 : 0); if (out == 8) return false;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].y > maxp.y) ? 1 : 0); if (out == 8) return false;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].y < minp.y) ? 1 : 0); if (out == 8) return false;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].z > maxp.z) ? 1 : 0); if (out == 8) return false;
        out = 0; for (int i = 0; i < 8; i++) out += ((points[i].z < minp.z) ? 1 : 0); if (out == 8) return false;

        return true;
    }
}
