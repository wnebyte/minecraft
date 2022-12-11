package com.github.wnebyte.minecraft.util;

import org.joml.Vector3f;

public class JColor {

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    public static final int BLACK_HEX = 0x000000;

    public static final int WHITE_HEX = 0xFFFFFF;

    public static final Vector3f YELLOW_VEC3 = new Vector3f(252f / 255f, 248f / 255f, 3f / 255f);

    public static final Vector3f WHITE_VEC3 = new Vector3f(1f, 1f, 1f);

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
