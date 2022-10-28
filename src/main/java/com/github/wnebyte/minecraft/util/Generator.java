package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class Generator {

    private final FastNoiseLite fnlState;

    private final float weight;

    public Generator(FastNoiseLite fnlState, float weight) {
        this.fnlState = fnlState;
        this.weight = weight;
    }

    public Generator(FnlState fnlState, int seed) {
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
        if (!(o instanceof Generator)) return false;
        Generator generator = (Generator) o;
        return Objects.equals(generator.fnlState, this.fnlState) &&
                Objects.equals(generator.weight, this.weight);
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
