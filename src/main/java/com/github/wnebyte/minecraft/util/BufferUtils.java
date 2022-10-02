package com.github.wnebyte.minecraft.util;

import java.nio.FloatBuffer;
import org.joml.Vector3f;

public class BufferUtils {

    private void sort(FloatBuffer buffer, Vector3f pos) {
        int capacity = buffer.capacity();
        for (int i = 0; i < capacity; i += 5) {
            float x = buffer.get(i), y = buffer.get(i + 1), z = buffer.get(i + 2);

        }
    }

    private void swap(FloatBuffer buffer, int i, int j) {
        float tmp = buffer.get(i);
        buffer.put(i, buffer.get(j));
        buffer.put(j, tmp);
    }
}
