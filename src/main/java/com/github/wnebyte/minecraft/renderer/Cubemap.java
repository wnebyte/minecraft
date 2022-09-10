package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.*;

public class Cubemap {

    public static int DAY = 0;

    public static int NIGHT = 1;

    private static final List<String> FACES_DAY = Arrays.asList(
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayRight.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayLeft.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayTop.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayBottom.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayBack.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/dayFront.png"
    );

    private static final List<String> FACES_NIGHT = Arrays.asList(
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightRight.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightLeft.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightTop.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightBottom.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightBack.png",
            "c:/users/ralle/dev/java/minecraft/assets/images/sky/nightFront.png"
    );

    private int id;

    public Cubemap(int type) {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        List<String> textures = (type == NIGHT) ? FACES_NIGHT : FACES_DAY;
        for (int i = 0; i < textures.size(); i++) {
            String path = textures.get(i);
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            stbi_set_flip_vertically_on_load(false);
            ByteBuffer image = stbi_load(path, width, height, channels, 0);

            if (image != null) {
                int w = width.get(0);
                int h = height.get(0);

                if (channels.get(0) == 3) {
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, w, h,
                            0, GL_RGB, GL_UNSIGNED_BYTE, image);
                } else if (channels.get(0) == 4) {
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, w, h,
                            0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                } else {
                    assert false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'.";
                }
            } else {
                assert false : "Error: (Texture) Could not load image '" + path + "'.";
            }

            stbi_image_free(image);
        }
    }

    public int getId() {
        return id;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }
}
