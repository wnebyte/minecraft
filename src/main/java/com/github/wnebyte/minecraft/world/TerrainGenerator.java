package com.github.wnebyte.minecraft.world;

import com.github.wnebyte.minecraft.util.FastNoiseLite;

public class TerrainGenerator {

    private FastNoiseLite noiseLite = new FastNoiseLite();

    private float[] noiseData = new float[128 * 128];

    public void init(int seed) {
        noiseLite.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noiseLite.SetSeed(seed);

        int index = 0;
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                noiseData[index++] = noiseLite.GetNoise(x, y);
            }
        }
    }

    public int getHeight(int x, int z, float min, float max) {
        float normalized = getNormalizedHeight(x, z);
        return 0;
    }

    public float getNormalizedHeight(int x, int z) {
        float[] noise = new float[noiseData.length];

        for (int i = 0; i < noiseData.length; i++) {
            noise[i] = noiseLite.GetNoise(x, z, i);
        }

        float blendedNoise = 0.0f;
        float weightSums = 0.0f;
        for (int i = 0; i < noise.length; i++) {
            blendedNoise += noise[i] * noiseData[i];
            weightSums += noiseData[i];
        }

        blendedNoise /= weightSums;
        blendedNoise = (float)Math.pow(blendedNoise, 1.19f);
        return blendedNoise;
    }
}
