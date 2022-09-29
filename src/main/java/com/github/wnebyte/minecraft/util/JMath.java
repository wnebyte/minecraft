package com.github.wnebyte.minecraft.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
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

    public static Vector4f sub(Vector4f a, Vector4f b) {
        Vector4f c = new Vector4f();
        c.x = a.x - b.x;
        c.y = a.y - b.y;
        c.z = a.z - b.z;
        c.w = a.w - b.w;
        return c;
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
}
