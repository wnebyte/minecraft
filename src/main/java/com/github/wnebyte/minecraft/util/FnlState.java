package com.github.wnebyte.minecraft.util;

import java.util.Objects;

/**
 * Is a wrapper class for data that needs to be deserialized from disk.
 */
public class FnlState {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static class Builder {

        private int id;

        private float weight;

        private FastNoiseLite.NoiseType noiseType;

        private float frequency;

        private FastNoiseLite.FractalType fractalType;

        private int octaves;

        private float lacunarity;

        private float gain;

        private float weightedStrength;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setWeight(float weight) {
            this.weight = weight;
            return this;
        }

        public Builder setNoiseType(FastNoiseLite.NoiseType noiseType) {
            this.noiseType = noiseType;
            return this;
        }

        public Builder setFrequeny(float frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setFractalType(FastNoiseLite.FractalType fractalType) {
            this.fractalType = fractalType;
            return this;
        }

        public Builder setOctaves(int octaves) {
            this.octaves = octaves;
            return this;
        }

        public Builder setLacunarity(float lacunarity) {
            this.lacunarity = lacunarity;
            return this;
        }

        public Builder setGain(float gain) {
            this.gain = gain;
            return this;
        }

        public Builder setWeightedStrength(float weightedStrength) {
            this.weightedStrength = weightedStrength;
            return this;
        }

        public FnlState build() {
            FnlState fnlState = new FnlState();
            fnlState.id = id;
            fnlState.weight = weight;
            fnlState.noiseType = noiseType;
            fnlState.frequency = frequency;
            fnlState.fractalType = fractalType;
            fnlState.octaves = octaves;
            fnlState.lacunarity = lacunarity;
            fnlState.gain = gain;
            fnlState.weightedStrength = weightedStrength;
            return fnlState;
        }
    }

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private int id;

    private float weight;

    // general

    private FastNoiseLite.NoiseType noiseType;

    private float frequency;

    // fractal

    private FastNoiseLite.FractalType fractalType;

    private int octaves;

    private float lacunarity;

    private float gain;

    private float weightedStrength;

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public FastNoiseLite.NoiseType getNoiseType() {
        return noiseType;
    }

    public void setNoiseType(FastNoiseLite.NoiseType noiseType) {
        this.noiseType = noiseType;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public FastNoiseLite.FractalType getFractalType() {
        return fractalType;
    }

    public void setFractalType(FastNoiseLite.FractalType fractalType) {
        this.fractalType = fractalType;
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getWeightedStrength() {
        return weightedStrength;
    }

    public void setWeightedStrength(float weightedStrength) {
        this.weightedStrength = weightedStrength;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof FnlState)) return false;
        FnlState fnlState = (FnlState) o;
        return Objects.equals(fnlState.id, this.id) &&
                Objects.equals(fnlState.weight, this.weight) &&
                Objects.equals(fnlState.noiseType, this.noiseType) &&
                Objects.equals(fnlState.frequency, this.frequency) &&
                Objects.equals(fnlState.fractalType, this.fractalType) &&
                Objects.equals(fnlState.octaves, this.octaves) &&
                Objects.equals(fnlState.lacunarity, this.lacunarity) &&
                Objects.equals(fnlState.gain, this.gain) &&
                Objects.equals(fnlState.weightedStrength, this.weightedStrength);
    }

    @Override
    public int hashCode() {
        int result = 39;
        return result +
                Objects.hashCode(this.id) +
                Objects.hashCode(this.weight) +
                Objects.hashCode(this.noiseType) +
                Objects.hashCode(this.frequency) +
                Objects.hashCode(this.fractalType) +
                Objects.hashCode(this.octaves) +
                Objects.hashCode(this.lacunarity) +
                Objects.hashCode(this.octaves) +
                Objects.hashCode(this.weightedStrength);
    }
}
