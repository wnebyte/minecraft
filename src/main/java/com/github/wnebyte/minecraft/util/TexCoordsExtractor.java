package com.github.wnebyte.minecraft.util;

import org.joml.Vector2f;

public interface TexCoordsExtractor {

    Vector2f[] apply(float x, float y, int width, int height, int texWidth, int texHeight);
}
