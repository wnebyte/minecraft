package com.github.wnebyte.minecraft.util;

public class ManhattanDistanceFunction implements DistanceFunction<Integer> {

    @Override
    public Integer get(Integer a, Integer b) {
        return (a + b);
    }
}
