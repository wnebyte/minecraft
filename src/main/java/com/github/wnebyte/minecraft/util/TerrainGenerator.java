package com.github.wnebyte.minecraft.util;

public class TerrainGenerator {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static TerrainGenerator getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException(
                    "TerrainGenerator has not been initialized."
            );
        }
    }

    public static void load(String terrainNoiseConfig, int seed) {
        if (instance != null) {
            throw new IllegalStateException(
                    "TerrainGenerator has already been initialized."
            );
        }
        assert Files.exists(terrainNoiseConfig) : "The config file does not exist.";
        String json = Files.read(terrainNoiseConfig);

        FnlState[] fnlStates = Settings.GSON.fromJson(json, FnlState[].class);
        assert (fnlStates.length > 0) : "Generators is empty";
        Generator[] generators = new Generator[fnlStates.length];

        for (int i = 0; i < fnlStates.length; i++) {
            FnlState fnlState = fnlStates[i];
            int newSeed = seed + (i * 7);
            Generator generator = new Generator(fnlState, newSeed);
            generators[i] = generator;
        }

        instance = new TerrainGenerator(generators, seed);
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

    private final Generator[] generators;

    private final int seed;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private TerrainGenerator(Generator[] generators, int seed) {
        this.generators = generators;
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

    public float getNormalizedHeight(int x, int z) {
        float blendedNoise = 0.0f;
        float weightSum = 0.0f;

        for (Generator generator : generators) {
            float noise = generator.getNoise(x, z);
            float weight = generator.getWeight();
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
