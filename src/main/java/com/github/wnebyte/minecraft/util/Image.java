package com.github.wnebyte.minecraft.util;

import java.util.Objects;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.stb.STBImage.*;

public class Image {

    private final String path;

    private final int width;

    private final int height;

    private final int channels;

    private final boolean flip;

    private final ByteBuffer data;

    public Image(String path) {
        this(path, 0);
    }

    public Image(String path, boolean flip) {
        this(path, 0, flip);
    }

    public Image(String path, int channels) {
        this(path, channels, false);
    }

    public Image(String path, int channels, boolean flip) {
        IntBuffer w = org.lwjgl.BufferUtils.createIntBuffer(1);
        IntBuffer h = org.lwjgl.BufferUtils.createIntBuffer(1);
        IntBuffer c = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(flip);
        this.data = stbi_load(path, w, h, c, channels);
        this.width = w.get(0);
        this.height = h.get(0);
        this.channels = c.get(0);
        this.path = path;
        this.flip = flip;
    }

    public String getPath() {
        return path;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannels() {
        return channels;
    }

    public boolean isFlipped() {
        return flip;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void free() {
        stbi_image_free(data);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Image)) return false;
        Image image = (Image)o;
        return Objects.equals(image.path, this.path) &&
                Objects.equals(image.width, this.width) &&
                Objects.equals(image.height, this.height) &&
                Objects.equals(image.channels, this.channels) &&
                Objects.equals(image.flip, this.flip);
    }

    @Override
    public int hashCode() {
        int result = 13;
        return 2 *
                result +
                Objects.hashCode(this.path) +
                Objects.hashCode(this.width) +
                Objects.hashCode(this.height) +
                Objects.hashCode(this.channels) +
                Objects.hashCode(this.flip);
    }

    @Override
    public String toString() {
        return String.format("Image[path: %s, width: %d, height: %d, channels: %d, flip: %s]",
                this.path, this.width, this.height, this.channels, this.flip);
    }
}
