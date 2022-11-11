package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.renderer.TextureFormat;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;

public class TexturePacker {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

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

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final TexCoordsExtractor BOTTOM_ORIGIN_TEX_COORDS_EXTRACTOR = new TexCoordsExtractor() {
        @Override
        public Vector2f[] apply(float x, float y, int width, int height, int texWidth, int texHeight) {
            float topY = (y + height) / texHeight;
            float rightX = (x + width) / texWidth;
            float leftX = x / texWidth;
            float bottomY = y / texHeight;
            return new Vector2f[]{
                    new Vector2f(rightX, topY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(leftX, topY)
            };
        }
    };

    public static final TexCoordsExtractor TOP_ORIGIN_TEX_COORDS_EXTRACTOR = new TexCoordsExtractor() {
        @Override
        public Vector2f[] apply(float x, float y, int width, int height, int texWidth, int texHeight) {
            float topY = y / texHeight;
            float rightX = (x + width) / texWidth;
            float leftX = x / texWidth;
            float bottomY = (y + height) / texHeight;
            return new Vector2f[]{
                    new Vector2f(rightX, topY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(leftX, topY)
            };
        }
    };

    private static final FilenameFilter PNG_FILTER = (dir, name) -> (name.endsWith(".png"));

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private TexCoordsExtractor extractor;

    private boolean flipOnRead;

    private boolean flipOnWrite;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public TexturePacker() {
        this(BOTTOM_ORIGIN_TEX_COORDS_EXTRACTOR, false, false);
    }

    public TexturePacker(TexCoordsExtractor extractor) {
        this(extractor, false, false);
    }

    public TexturePacker(boolean flipOnRead, boolean flipOnWrite) {
        this(BOTTOM_ORIGIN_TEX_COORDS_EXTRACTOR, flipOnRead, flipOnWrite);
    }

    public TexturePacker(TexCoordsExtractor extractor, boolean flipOnRead, boolean flipOnWrite) {
        this.extractor = extractor;
        this.flipOnRead = flipOnRead;
        this.flipOnWrite = flipOnWrite;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void pack(String path, String configPath, String outputPath, boolean generateMips, int texWidth, int texHeight) {
        File dir = new File(path);
        if (Files.exists(configPath) && Files.exists(outputPath)) {
            File outputFile = new File(outputPath);
            if (outputFile.lastModified() > dir.lastModified()) {
                return;
            }
        }
        List<TextureLocation> locations = new ArrayList<>();
        int numFiles = dir.listFiles(PNG_FILTER).length;
        int pngOutputWidth = (int)Math.sqrt(numFiles * texWidth * texHeight);
        int currentX = 0;
        int currentY = 0;
        int lineHeight = 0;
        int numTexturesUsed = 0;

        // Generate the first output image
        List<Pixel> pixels = new ArrayList<>(pngOutputWidth);
        for (File image : dir.listFiles(PNG_FILTER)) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            stbi_set_flip_vertically_on_load(flipOnRead);
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
            locations.add(new TextureLocation(
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
        stbi_flip_vertically_on_write(flipOnWrite);
        stbi_write_png(outputPath, pngOutputWidth, pngOutputHeight, 4, data, pngOutputWidth * 4);

        TextureFormat[] textureFormats = new TextureFormat[locations.size()];
        int i = 0;
        for (TextureLocation location : locations) {
            Vector2f[] uvs = extractor.apply(
                    location.getX(), location.getY(),
                    location.getWidth(), location.getHeight(),
                    pngOutputWidth, pngOutputHeight);
            textureFormats[i] = new TextureFormat(i, location.getName(), uvs);
            i++;
        }

        // write texture formats to fs
        String json = Settings.GSON.toJson(textureFormats);
        Files.write(configPath, json);
    }

    public void setTexCoordsExtractor(TexCoordsExtractor extractor) {
        this.extractor = extractor;
    }

    public void setFlipOnRead(boolean value) {
        this.flipOnRead = value;
    }

    public void setFlipOnWrite(boolean value) {
        this.flipOnWrite = value;
    }
}
