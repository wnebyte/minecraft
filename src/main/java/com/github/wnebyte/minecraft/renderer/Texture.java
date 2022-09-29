package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.awt.image.BufferedImage;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private int id;

    private String path;

    private int width;

    private int height;

    public Texture(String path, boolean pixelate) {
        this.path = path;

        // Generate texture on GPU
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Set the texture parameters
        // Repeat the image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, pixelate ? GL_NEAREST : GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, pixelate ? GL_NEAREST : GL_LINEAR);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(path, width, height, channels, 0);

        if (image != null) {
            this.width = width.get(0);
            this.height = height.get(0);

            if (channels.get(0) == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, this.width, this.height,
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height,
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'.";
            }
        } else {
            assert false : "Error: (Texture) Could not load image '" + path + "'.";
        }

        stbi_image_free(image);
    }

    public Texture(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.path = "BufferedImage";
        int[] pixels = new int[height * width];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                byte alpha = (byte)((pixel >> 24) & 0xFF);
                for (int i = 0; i < 4; i++) {
                    buffer.put(alpha);
                }
            }
        }
        buffer.flip();

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        buffer.clear();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Texture)) return false;
        Texture texture = (Texture) o;
        return Objects.equals(texture.id, this.id) &&
                Objects.equals(texture.path, this.path) &&
                Objects.equals(texture.width, this.width) &&
                Objects.equals(texture.height, this.height);
    }

    @Override
    public int hashCode() {
        int result = 65;
        return result +
                17 +
                Objects.hashCode(id) +
                Objects.hashCode(path) +
                Objects.hashCode(width) +
                Objects.hashCode(height);
    }

    @Override
    public String toString() {
        return String.format(
                "Texture[id: %s, path: %s, width: %d, height: %d]", id, path, width, height
        );
    }
}
