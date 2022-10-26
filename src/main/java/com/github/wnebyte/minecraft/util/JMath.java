package com.github.wnebyte.minecraft.util;

import java.lang.Math;
import org.joml.*;

public class JMath {

    public static Vector4f add(Vector4f a, Vector4f b) {
        Vector4f c = new Vector4f();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z + b.z;
        c.w = a.w + b.w;
        return c;
    }

    public static Vector3f add(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z + b.z;
        return c;
    }

    public static Vector3f add(Vector3f vec, float val) {
        Vector3f v = new Vector3f();
        v.x = vec.x + val;
        v.y = vec.y + val;
        v.z = vec.z + val;
        return v;
    }

    public static void addX(Vector3f v, float val) {
        v.x += val;
    }

    public static void addY(Vector3f v, float val) {
        v.y += val;
    }

    public static void addZ(Vector3f v, float val) {
        v.z += val;
    }

    public static Vector4f sub(Vector4f a, Vector4f b) {
        Vector4f c = new Vector4f();
        c.x = a.x - b.x;
        c.y = a.y - b.y;
        c.z = a.z - b.z;
        c.w = a.w - b.w;
        return c;
    }

    public static Vector3f sub(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f();
        c.x = a.x - b.x;
        c.y = a.y - b.y;
        c.z = a.z - b.z;
        return c;
    }

    public static Vector3f div(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f();
        c.x = a.x / b.x;
        c.y = a.y / b.y;
        c.z = a.z / b.z;
        return c;
    }

    public static Vector3f mul(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f();
        c.x = a.x * b.x;
        c.y = a.y * b.y;
        c.z = a.z * b.z;
        return c;
    }

    public static Vector3f mul(Vector3f vec, float scalar) {
        Vector3f v = new Vector3f();
        v.x = vec.x * scalar;
        v.y = vec.y * scalar;
        v.z = vec.z * scalar;
        return v;
    }

    public static void subX(Vector3f v, float val) {
        v.x -= val;
    }

    public static void subY(Vector3f v, float val) {
        v.y -= val;
    }

    public static void subZ(Vector3f v, float val) {
        v.z -= val;
    }

    public static Matrix4f mul(Matrix4f a, Matrix4f b) {
        Matrix4f ma = new Matrix4f(a);
        Matrix4f mb = new Matrix4f(b);
        return ma.mul(mb);
    }

    public static Vector3f toVector3f(Vector4f vec) {
        Vector3f v = new Vector3f();
        v.x = vec.x;
        v.y = vec.y;
        v.z = vec.z;
        return v;
    }

    public static Vector3f toVector3f(Vector3i vec) {
        Vector3f v = new Vector3f();
        v.x = vec.x;
        v.y = vec.y;
        v.z = vec.z;
        return v;
    }

    public static Vector3f toVector3f(Vector2i vec, float z) {
        Vector3f v = new Vector3f();
        v.x = vec.x;
        v.y = vec.y;
        v.z = z;
        return v;
    }

    public static float max(float a, float b, float c) {
        return Math.max(a, Math.max(b, c));
    }

    public static float absMax(float a, float b, float c) {
        return Math.max(Math.abs(a), Math.max(Math.abs(b), Math.abs(c)));
    }

    public static float max(Vector3f v) {
        return max(v.x, v.y, v.z);
    }

    public static float absMax(Vector3f vec) {
        return max(vec.x, vec.y, vec.z);
    }

    public static Vector3f abs(Vector3f v) {
        Vector3f vec = new Vector3f();
        vec.x = Math.abs(v.x);
        vec.y = Math.abs(v.y);
        vec.z = Math.abs(v.z);
        return vec;
    }

    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        return Math.min(val, max);
    }

    public static float clamp(float val, float min, float max) {
        if (val < min) return min;
        return Math.min(val, max);
    }

    public static void rotate(Vector2f vec, float angleDeg, Vector2f origin) {
        float x = vec.x - origin.x;
        float y = vec.y - origin.y;

        float cos = (float)Math.cos(Math.toRadians(angleDeg));
        float sin = (float)Math.sin(Math.toRadians(angleDeg));

        float xPrime = (x * cos) - (y * sin);
        float yPrime = (x * sin) + (y * cos);

        xPrime += origin.x;
        yPrime += origin.y;

        vec.x = xPrime;
        vec.y = yPrime;
    }

    public static void rotate(Vector3f vec, float angleDeg, Vector3f origin) {
        float x = vec.x - origin.x;
        float y = vec.y - origin.y;

        float cos = (float)Math.cos(Math.toRadians(angleDeg));
        float sin = (float)Math.sin(Math.toRadians(angleDeg));

        float xPrime = (x * cos) - (y * sin);
        float yPrime = (x * sin) + (y * cos);

        xPrime += origin.x;
        yPrime += origin.y;

        vec.x = xPrime;
        vec.y = yPrime;
    }


    public static Vector3f ceil(Vector3f vec) {
        Vector3f v = new Vector3f();
        v.x = (float)Math.ceil(vec.x);
        v.y = (float)Math.ceil(vec.y);
        v.z = (float)Math.ceil(vec.z);
        return v;
    }

    public static Vector3f floor(Vector3f vec) {
        Vector3f v = new Vector3f();
        v.x = (float)Math.floor(vec.x);
        v.y = (float)Math.floor(vec.y);
        v.z = (float)Math.floor(vec.z);
        return v;
    }

    public static Vector3f round(Vector3f vec) {
        Vector3f v = new Vector3f();
        if (Float.compare((float)Math.ceil(vec.x) - vec.x, 0.5f) < 0) {
            v.x = (float)Math.ceil(v.x);
        } else {
            v.x = (float)Math.floor(v.x);
        }
        if (Float.compare((float)Math.ceil(vec.y) - vec.y, 0.5f) < 0) {
            v.y = (float)Math.ceil(vec.y);
        } else {
            v.y = (float)Math.floor(vec.y);
        }
        if (Float.compare((float)Math.ceil(vec.z) - vec.z, 0.5f) < 0) {
            v.z = (float)Math.ceil(vec.z);
        } else {
            v.z = (float)Math.floor(vec.z);
        }
        return v;
    }

    public static Vector3f sign(Vector3f vec) {
        Vector3f v = new Vector3f();
        v.x = Math.signum(vec.x);
        v.y = Math.signum(vec.y);
        v.z = Math.signum(vec.z);
        return v;
    }

    public static void compareAndSet(Vector3f vec, float comparable, float val) {
        if (vec.x == comparable) {
            vec.x = val;
        }
        if (vec.y == comparable) {
            vec.y = val;
        }
        if (vec.z == comparable) {
            vec.z = val;
        }
    }

    public static boolean compare(float x, float y) {
        return Math.abs(x - y) <= Float.MIN_VALUE * Math.max(1.0f, Math.max(Math.abs(x), Math.abs(y)));
    }

    public static boolean compare(float x, float y, float epsilon) {
        return Math.abs(x - y) <= epsilon * Math.max(1.0f, Math.max(Math.abs(x), Math.abs(y)));
    }

    public static boolean compare(Vector2f vec1, Vector2f vec2, float epsilon) {
        return compare(vec1.x, vec2.x, epsilon) && compare(vec1.y, vec2.y, epsilon);
    }

    public static boolean compare(Vector3f vec1, Vector3f vec2, float epsilon) {
        return compare(vec1.x, vec2.x, epsilon) && compare(vec1.y, vec2.y, epsilon) && compare(vec1.z, vec2.z, epsilon);
    }

    public static boolean compare(Vector4f vec1, Vector4f vec2, float epsilon) {
        return compare(vec1.x, vec2.x, epsilon) && compare(vec1.y, vec2.y, epsilon) && compare(vec1.z, vec2.z, epsilon) && compare(vec1.w, vec2.w, epsilon);
    }


    public static boolean compare(Vector2f vec1, Vector2f vec2) {
        return compare(vec1.x, vec2.x) && compare(vec1.y, vec2.y);
    }
}
