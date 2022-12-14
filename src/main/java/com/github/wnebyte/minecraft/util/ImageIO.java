package com.github.wnebyte.minecraft.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
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

    public static ByteBuffer read(String path, int channels) {
        return read(path, channels, false);
    }

    public static ByteBuffer read(String path, int channels, boolean flip) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer c = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(flip);
        return stbi_load(path, w, h, c, channels);
    }
}
