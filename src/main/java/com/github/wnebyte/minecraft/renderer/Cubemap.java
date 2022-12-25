package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.*;

public class Cubemap extends Texture {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public enum Type {
        DAY,
        NIGHT;
    }

    public static class Configuration {

        private List<String> faces;

        private List<Texture.Parameter> parameters;

        private Configuration(List<String> faces, List<Texture.Parameter> parameters) {
            this.faces = faces;
            this.parameters = parameters;
        }

        public List<String> getFaces() {
            return faces;
        }

        public List<Texture.Parameter> getParameters() {
            return parameters;
        }

        public static class Builder {

            private List<String> faces = new ArrayList<>();

            private List<Texture.Parameter> parameters = new ArrayList<>();

            public Builder setFaces(List<String> faces) {
                this.faces = faces;
                return this;
            }

            public Builder addFace(String... faces) {
                this.faces.addAll(Arrays.asList(faces));
                return this;
            }

            public Builder setParameters(List<Texture.Parameter> parameters) {
                this.parameters = parameters;
                return this;
            }

            public Builder addParameter(Texture.Parameter... parameters) {
                this.parameters.addAll(Arrays.asList(parameters));
                return this;
            }

            public Builder addParameter(int name, int value) {
                this.parameters.add(new Texture.Parameter(name, value));
                return this;
            }

            public Configuration build() {
                return new Configuration(faces, parameters);
            }
        }
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final List<String> DEFAULT_FACES_DAY = Arrays.asList(
            Assets.DIR + "/images/sky/dayRight.png",
            Assets.DIR + "/images/sky/dayLeft.png",
            Assets.DIR + "/images/sky/dayTop.png",
            Assets.DIR + "/images/sky/dayBottom.png",
            Assets.DIR + "/images/sky/dayBack.png",
            Assets.DIR + "/images/sky/dayFront.png"
    );

    public static final List<String> DEFAULT_FACES_NIGHT = Arrays.asList(
            Assets.DIR + "/images/sky/nightRight.png",
            Assets.DIR + "/images/sky/nightLeft.png",
            Assets.DIR + "/images/sky/nightTop.png",
            Assets.DIR + "/images/sky/nightBottom.png",
            Assets.DIR + "/images/sky/nightBack.png",
            Assets.DIR + "/images/sky/nightFront.png"
    );

    public static final List<Texture.Parameter> DEFAULT_TEXTURE_PARAMS = Arrays.asList(
            new Texture.Parameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR),
            new Texture.Parameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR),
            new Texture.Parameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE),
            new Texture.Parameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE),
            new Texture.Parameter(GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
    );

    public static final List<Texture.Parameter> PIXELATED_TEXTURE_PARAMS = Arrays.asList(
            new Texture.Parameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST),
            new Texture.Parameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST),
            new Texture.Parameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE),
            new Texture.Parameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE),
            new Texture.Parameter(GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
    );

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private int id;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Cubemap(Type type) {
        this((type == Type.DAY) ? DEFAULT_FACES_DAY : DEFAULT_FACES_NIGHT, PIXELATED_TEXTURE_PARAMS);
    }

    public Cubemap(List<String> faces, List<Texture.Parameter> params) {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        for (Texture.Parameter param : params) {
            glTexParameteri(GL_TEXTURE_CUBE_MAP, param.getName(), param.getValue());
        }

        List<String> textures = faces;
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
                    assert false : "Error: (Cubemap) Unknown number of channels '" + channels.get(0) + "'.";
                }
            } else {
                assert false : "Error: (Cubemap) Could not load image '" + path + "'.";
            }

            stbi_image_free(image);
        }
    }

    public Cubemap(int width, int height, List<Texture.Parameter> params) {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        for (Texture.Parameter param : params) {
            glTexParameteri(GL_TEXTURE_CUBE_MAP, param.getName(), param.getValue());
        }

        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT, width, height,
                    0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        }
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

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
