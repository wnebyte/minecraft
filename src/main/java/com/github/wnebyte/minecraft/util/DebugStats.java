package com.github.wnebyte.minecraft.util;

public class DebugStats {

    /** number of draw calls */
    public static long drawCalls;

    /** mem allocated for all subchunks */
    public static long vertexMemAlloc;

    /** mem used for all subchunks */
    public static long vertexMemUsed;

    public static void reset() {
        drawCalls = 0L;
        vertexMemAlloc = 0L;
        vertexMemUsed = 0L;
    }
}
