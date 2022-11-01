package com.github.wnebyte.minecraft.util;

public class TerrainGenerator {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static TerrainGenerator getInstance() {
        if (TerrainGenerator.instance != null) {
            return TerrainGenerator.instance;
        } else {
            throw new IllegalStateException(
                    "TerrainGenerator has not been initialized."
            );
        }
    }

    public static void load(String terrainNoiseConfig, int seed) {
        if (TerrainGenerator.instance != null) {
            throw new IllegalStateException(
                    "TerrainGenerator has already been initialized."
            );
        }
        assert Files.exists(terrainNoiseConfig) : "The terrainNoiseConfig file does not exist.";
        String json = Files.read(terrainNoiseConfig);
        FnlState[] fnlStates = Settings.GSON.fromJson(json, FnlState[].class);
        assert (fnlStates.length > 0) : "The terrainNoiseConfig is empty";
        NoiseGenerator[] noiseGenerators = new NoiseGenerator[fnlStates.length];

        for (int i = 0; i < fnlStates.length; i++) {
            FnlState fnlState = fnlStates[i];
            int newSeed = seed + (i * 7);
            NoiseGenerator noiseGenerator = new NoiseGenerator(fnlState, newSeed);
            noiseGenerators[i] = noiseGenerator;
        }

        TerrainGenerator.instance = new TerrainGenerator(noiseGenerators, seed);
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static TerrainGenerator instance;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final NoiseGenerator[] noiseGenerators;

    private final int seed;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private TerrainGenerator(NoiseGenerator[] noiseGenerators, int seed) {
        this.noiseGenerators = noiseGenerators;
        this.seed = seed;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public int getHeight(int x, int z, float min, float max) {
        float normalizedHeight = getNormalizedHeight(x, z);
        return (int)JMath.mapRange(normalizedHeight, 0.0f, 1.0f, min, max);
    }

    private float getNormalizedHeight(int x, int z) {
        float blendedNoise = 0.0f;
        float weightSum = 0.0f;

        for (NoiseGenerator noiseGenerator : noiseGenerators) {
            float noise = noiseGenerator.getNoise(x, z);
            float weight = noiseGenerator.getWeight();
            blendedNoise += noise * weight;
            weightSum += weight;
        }

        // Divide by the weight of the sums to normalize the value again
        blendedNoise /= weightSum;

        // Raise it to a power to flatten valleys and increase mountains
        blendedNoise = (float)Math.pow(blendedNoise, 1.19f);

        return blendedNoise;
    }

    public int getSeed() {
        return seed;
    }
}
