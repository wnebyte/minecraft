package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

public class TexturePacker {

    private static class Location {

        private String name;

        private float x, y, width, height;

        private Location(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class Pixel {

        private byte r, g, b, a;
    }

    private static void resize(List<Pixel> pixels, int n) {
        int size = pixels.size();
        if (n < size) {
            for (int i = n; i < size; i++) {
                pixels.remove(i);
                i--;
            }
        } else {
            for (int i = size; i < n; i++) {
                pixels.add(new Pixel());
            }
        }
    }

    private static ByteBuffer toByteBuffer(List<Pixel> pixels) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.size() * 4);
        for (Pixel pixel : pixels) {
            buffer.put(pixel.r);
            buffer.put(pixel.g);
            buffer.put(pixel.b);
            buffer.put(pixel.a);
        }
        buffer.rewind();
        return buffer;
    }

    private static final FilenameFilter PNG_FILTER = (dir, name) -> (name.endsWith(".png"));

    public static void pack(String path, String configPath, String outputPath, boolean generateMips, int texWidth, int texHeight) {
        File file = new File(path);
        if (Files.exists(configPath) && Files.exists(outputPath)) {
            File outputFile = new File(outputPath);
            if (outputFile.lastModified() > file.lastModified()) {
                return;
            }
        }
        List<Location> locations = new ArrayList<>();
        int numFiles = file.listFiles(PNG_FILTER).length;
        int pngOutputWidth = (int)Math.sqrt(numFiles * texWidth * texHeight);
        int currentX = 0;
        int currentY = 0;
        int lineHeight = 0;
        int numTexturesUsed = 0;

        // Generate the first output image
        List<Pixel> pixels = new ArrayList<>(pngOutputWidth);
        for (File image : file.listFiles(PNG_FILTER)) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            stbi_set_flip_vertically_on_load(true);
            ByteBuffer rawPixels = stbi_load(image.getPath(), width, height, channels, 4);

            int w = width.get(0);
            int h = height.get(0);
            // Shrink the image height if it's bigger than the normal texture height
            h = Math.min(h, texHeight);
            int newX = currentX + w;
            if (newX >= pngOutputWidth) {
                currentY += lineHeight;
                currentX = 0;
                lineHeight = 0;
            }

            if (h > lineHeight) {
                lineHeight = h;
                resize(pixels, (currentY + lineHeight + 1) * pngOutputWidth);
            }

            // Save the current x and current y and width and height and filename
            locations.add(new Location(
                    image.getName().split(".png")[0],
                    currentX, currentY,
                    w, h));

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int localX = x + currentX;
                    int localY = y + currentY;
                    Pixel currentPixel = pixels.get(localY * pngOutputWidth + localX);
                    currentPixel.r = rawPixels.get(((y * w + x) * 4));
                    currentPixel.g = rawPixels.get(((y * w + x) * 4) + 1);
                    currentPixel.b = rawPixels.get(((y * w + x) * 4) + 2);
                    currentPixel.a = rawPixels.get(((y * w + x) * 4) + 3);
                }
            }

            stbi_image_free(rawPixels);
            numTexturesUsed++;
            currentX += w;
        }

        int pngOutputHeight = currentY + lineHeight;
        ByteBuffer data = toByteBuffer(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(outputPath, pngOutputWidth, pngOutputHeight, 4, data, pngOutputWidth * 4);

        /*
        if (generateMips) {
            // Figure out how many mip levels we need and generate mipped versions of the files
            int numMipLevels = (int)Math.floor(Math.log(Math.min(pngOutputWidth / texWidth, pngOutputHeight / texHeight))) + 1;
            int[][] mipImages = new int[numMipLevels][4];
            int[] widths = new int[numMipLevels];
            int[] heights = new int[numMipLevels];
            int[] texWidths = new int[numMipLevels];
            int[] texHeights = new int[numMipLevels];
            for (int i = 0; i < numMipLevels; i++) {
                int newWidth = Math.max(pngOutputWidth >> (i + 1), 1);
                int newHeight = Math.max(pngOutputHeight >> (i + 1), 1);
                int newTexWidth = Math.max(texWidth >> (i + 1), 0);
                int newTexHeight = Math.max(texHeight >> (i + 1), 0);
                widths[i] = newWidth;
                heights[i] = newHeight;
                texWidths[i] = newTexWidth;
                texHeights[i] = newTexHeight;
            }

            int index = 0;
            int numTexturesPerRow = pngOutputWidth / texWidth;
            for (File image : file.listFiles(PNG_FILTER)) {
                IntBuffer width = BufferUtils.createIntBuffer(1);
                IntBuffer height = BufferUtils.createIntBuffer(1);
                IntBuffer channels = BufferUtils.createIntBuffer(1);
                ByteBuffer rawPixels = stbi_load(image.getPath(), width, height, channels, 4);
                int w = width.get(0);
                int h = height.get(0);
                h = Math.min(h, texHeight);

                for (int i = 0; i < numMipLevels; i++) {
                    int imageWidth = widths[i];
                    int imageHeight = heights[i];
                    int xPos = (index % numTexturesPerRow) * texWidths[i];
                    int yPos = (index / numTexturesPerRow) * texHeights[i];
                }
            }
        }
         */

        TextureFormat[] textureFormats = new TextureFormat[locations.size()];
        int i = 0;
        for (Location location : locations) {
            Vector2f[] uvs = {
                    // TR
                    new Vector2f((location.x + location.width) / pngOutputWidth, location.y / pngOutputHeight),
                    // BR
                    new Vector2f((location.x + location.width) / pngOutputWidth, (location.y + location.height) / pngOutputHeight),
                    // BL
                    new Vector2f(location.x / pngOutputWidth, (location.y + location.height) / pngOutputHeight),
                    // TL
                    new Vector2f(location.x / pngOutputWidth, location.y / pngOutputHeight)
            };
            textureFormats[i] = new TextureFormat(i, location.name, uvs);
            i++;
        }

        // write texture formats to fs
        String json = Settings.GSON.toJson(textureFormats);
        Files.write(configPath, json);
    }
}
