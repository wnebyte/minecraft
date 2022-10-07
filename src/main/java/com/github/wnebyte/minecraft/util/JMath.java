package com.github.wnebyte.minecraft.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

public class JMath {

    public static Vector4f add(Vector4f a, Vector4f b) {
        Vector4f c = new Vector4f();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z + b.z;
        c.w = a.w = b.w;
        return c;
    }

    public static Vector3f add(Vector3f a, Vector3f b) {
        Vector3f c = new Vector3f();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z + b.z;
        return c;
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

    public static float max(float a, float b, float c) {
        return Math.max(a, Math.max(b, c));
    }

    public static float max(Vector3f v) {
        return max(v.x, v.y, v.z);
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

    public static int compare(Vector3f a, Vector3f b) {
        return 0;
    }
}
