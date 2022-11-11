package com.github.wnebyte.minecraft.util;

import java.nio.ByteBuffer;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;

public class ImageIO {

    public static void write(String path, int width, int height, int channels, ByteBuffer data) {
        write(path, width, height, channels, data, width * channels, false);
    }

    public static void write(String path, int width, int height, int channels, ByteBuffer data, boolean flip) {
        write(path, width, height, channels, data, width * channels, flip);
    }

    public static void write(String path, int width, int height, int channels, ByteBuffer data, int stride) {
        write(path, width, height, channels, data, stride, false);
    }

    public static void write(String path, int width, int height, int channels, ByteBuffer data, int stride, boolean flip) {
        stbi_flip_vertically_on_write(flip);
        stbi_write_png(path, width, height, channels, data, stride);
    }
}
