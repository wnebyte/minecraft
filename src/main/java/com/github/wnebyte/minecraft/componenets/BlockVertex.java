package com.github.wnebyte.minecraft.componenets;

import com.github.wnebyte.minecraft.renderer.VertexBuffer;

public class BlockVertex {

    public static int[] indexToTextureShifted;

    public static void AppendQuadX(VertexBuffer buffer, int x, int yL, int yR, int kL, int kR, int normal, int textureHealth)
    {
        /*
        int shared = x |
                textureHealth |
                normal;

        buffer.data[buffer.used]     = yR | kL | shared;
        buffer.data[buffer.used + 1] = buffer.data[buffer.used + 4] = yL | kL | shared;
        buffer.data[buffer.used + 2] = buffer.data[buffer.used + 3] = yR | kR | shared;
        buffer.data[buffer.used + 5] = yL | kR | shared;

        buffer.used += 6;
        System.out.println("AppendQuadX");

         */
    }

    public static void AppendQuadY(VertexBuffer buffer, int xL, int xR, int y, int zL, int zR, int normal, int textureHealth)
    {
        /*
        int shared = y |
                textureHealth |
                normal;

        buffer.data[buffer.used]     = xL |zR | shared;
        buffer.data[buffer.used + 1] = buffer.data[buffer.used + 4] = xL | zL | shared;
        buffer.data[buffer.used + 2] = buffer.data[buffer.used + 3] = xR | zR | shared;
        buffer.data[buffer.used + 5] = xR | zL | shared;

        buffer.used += 6;
        System.out.println("AppendQuadY");

         */
    }

    public static void AppendQuadZ(VertexBuffer buffer, int xL, int xR, int yL, int yR, int z, int normal, int textureHealth)
    {
        /*
        int shared = z |
                textureHealth |
                normal;

        buffer.data[buffer.used]     = xR | yR | shared;
        buffer.data[buffer.used + 1] = buffer.data[buffer.used + 4] = xR | yL | shared;
        buffer.data[buffer.used + 2] = buffer.data[buffer.used + 3] = xL | yR | shared;
        buffer.data[buffer.used + 5] = xL | yL | shared;

        buffer.used += 6;
        System.out.println("AppendQuadZ");

         */
    }
}
