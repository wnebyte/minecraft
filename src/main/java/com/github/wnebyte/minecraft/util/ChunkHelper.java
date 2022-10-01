package com.github.wnebyte.minecraft.util;

import com.github.wnebyte.minecraft.world.Chunk;

import java.util.Arrays;

public class ChunkHelper {

    public boolean[] visitedXN = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public boolean[] visitedXP = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public boolean[] visitedYN = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public boolean[] visitedYP = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public boolean[] visitedZN = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public boolean[] visitedZP = new boolean[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];

    public void reset() {
        Arrays.fill(visitedXN, false);
        Arrays.fill(visitedXP, false);
        Arrays.fill(visitedYN, false);
        Arrays.fill(visitedYP, false);
        Arrays.fill(visitedZN, false);
        Arrays.fill(visitedZP, false);
    }
}
