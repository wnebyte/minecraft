package com.github.wnebyte.minecraft.util;

import org.joml.Vector3f;

public class JColor {

    public static int toInt(Vector3f rgb) {
        int value = (((int)rgb.x & 0xFF) << 16) |
                    (((int)rgb.y & 0xFF) << 8)  |
                    (((int)rgb.z & 0xFF) << 0);
        return value;
    }

    public static Vector3f toVec3f(int rgb) {
        float r = (float)((rgb >> 16) & 0xFF) / 255.0f; // 0xFF0000
        float g = (float)((rgb >> 8)  & 0xFF) / 255.0f; // 0xFF00
        float b = (float)((rgb >> 0)  & 0xFF) / 255.0f; // 0xFF
        return new Vector3f(r, g, b);
    }
}
