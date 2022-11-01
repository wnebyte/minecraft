package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class NoiseGenerator {

    private final FastNoiseLite fnlState;

    private final float weight;

    public NoiseGenerator(FastNoiseLite fnlState, float weight) {
        this.fnlState = fnlState;
        this.weight = weight;
    }

    public NoiseGenerator(FnlState fnlState, int seed) {
        this.fnlState = new FastNoiseLite(seed);
        this.fnlState.SetNoiseType(fnlState.getNoiseType());
        this.fnlState.SetFrequency(fnlState.getFrequency());
        this.fnlState.SetFractalType(fnlState.getFractalType());
        this.fnlState.SetFractalOctaves(fnlState.getOctaves());
        this.fnlState.SetFractalLacunarity(fnlState.getLacunarity());
        this.fnlState.SetFractalGain(fnlState.getGain());
        this.fnlState.SetFractalWeightedStrength(fnlState.getWeightedStrength());
        this.weight = fnlState.getWeight();
    }

    public float getNoise(int x, int z) {
        return JMath.mapRange(fnlState.GetNoise(x, z), -1.0f, 1.0f, 0.0f, 1.0f);
    }

    public FastNoiseLite getFnlState() {
        return fnlState;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof NoiseGenerator)) return false;
        NoiseGenerator noiseGenerator = (NoiseGenerator) o;
        return Objects.equals(noiseGenerator.fnlState, this.fnlState) &&
                Objects.equals(noiseGenerator.weight, this.weight);
    }

    @Override
    public int hashCode() {
        int result = 41;
        return 2 *
                result +
                Objects.hashCode(this.fnlState) +
                Objects.hashCode(this.weight);
    }
}
