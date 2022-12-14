package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.awt.image.BufferedImage;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.util.Image;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static class Parameter {

        private int name;

        private int value;

        public Parameter(int name, int value) {
            this.name = name;
            this.value = value;
        }

        public int getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (!(o instanceof Parameter)) return false;
            Parameter param = (Parameter) o;
            return Objects.equals(param.name, this.name) &&
                    Objects.equals(param.value, this.value);
        }

        @Override
        public int hashCode() {
            int result = 13;
            return 3 *
                    result +
                    Objects.hashCode(this.name) +
                    Objects.hashCode(this.value);
        }

        public static class Builder {

            private int name;

            private int value;

            public Parameter.Builder setName(int pname) {
                this.name = pname;
                return this;
            }

            public Parameter.Builder setValue(int value) {
                this.value = value;
                return this;
            }

            public Parameter build() {
                return new Parameter(name, value);
            }
        }
    }

    public static class Configuration {

        private int target;

        private int level;

        private int internalFormat;

        private int width;

        private int height;

        private int border;

        private int format;

        private int type;

        private List<Parameter> parameters;

        private String path;

        private boolean flip;

        private Configuration() {}

        public boolean hasParameters() {
            return (parameters != null) && !(parameters.isEmpty());
        }

        public int getTarget() {
            return target;
        }

        public int getLevel() {
            return level;
        }

        public int getInternalFormat() {
            return internalFormat;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getBorder() {
            return border;
        }

        public int getFormat() {
            return format;
        }

        public int getType() {
            return type;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public String getPath() {
            return path;
        }

        public boolean isFlip() {
            return flip;
        }

        public static class Builder {

            public static final int DEFAULT_TARGET = GL_TEXTURE_2D;

            public static final int DEFAULT_LEVEL = 0;

            public static final int DEFAULT_INTERNAL_FORMAT = 0;

            public static final int DEFAULT_BORDER = 0;

            public static final int DEFAULT_FORMAT = 0;

            public static final int DEFAULT_TYPE = 0;

            private int target = DEFAULT_TARGET;

            private int level = DEFAULT_LEVEL;

            private int internalFormat = DEFAULT_INTERNAL_FORMAT;

            private int width;

            private int height;

            private int border = DEFAULT_BORDER;

            private int format = DEFAULT_FORMAT;

            private int type = DEFAULT_TYPE;

            private List<Parameter> parameters = new ArrayList<>();

            private String path;

            private boolean flip = false;

            public Builder setTarget(int value) {
                this.target = value;
                return this;
            }

            public Builder setLevel(int value) {
                this.level = value;
                return this;
            }

            public Builder setInternalFormat(int value) {
                this.internalFormat = value;
                return this;
            }

            public Builder setWidth(int value) {
                this.width = value;
                return this;
            }

            public Builder setHeight(int value) {
                this.height = value;
                return this;
            }

            public Builder setSize(int width, int height) {
                this.width = width;
                this.height = height;
                return this;
            }

            public Builder setBorder(int value) {
                this.border = value;
                return this;
            }

            public Builder setFormat(int value) {
                this.format = value;
                return this;
            }

            public Builder setType(int value) {
                this.type = value;
                return this;
            }

            public Builder setParameters(List<Parameter> parameters) {
                this.parameters = parameters;
                return this;
            }

            public Builder addParameter(int name, int value) {
                this.parameters.add(new Parameter(name, value));
                return this;
            }

            public Builder setPath(String path) {
                this.path = path;
                return this;
            }

            public Builder flip() {
                this.flip = true;
                return this;
            }

            public Configuration build() {
                Configuration conf = new Configuration();
                conf.target = target;
                conf.level = level;
                conf.internalFormat = internalFormat;
                conf.width = width;
                conf.height = height;
                conf.border = border;
                conf.format = format;
                conf.type = type;
                conf.parameters = parameters;
                conf.path = path;
                conf.flip = flip;
                return conf;
            }
        }
    }

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private int id;

    private int target;

    private String path;

    private int width;

    private int height;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Texture(String path, boolean pixelate) {
        this.target = GL_TEXTURE_2D;
        this.path = path;

        // Generate texture on GPU
        this.id = glGenTextures();
        glBindTexture(target, id);

        // Set the texture parameters
        // Repeat the image in both directions
        glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, pixelate ? GL_NEAREST : GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, pixelate ? GL_NEAREST : GL_LINEAR);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(false);
        ByteBuffer image = stbi_load(path, width, height, channels, 0);

        if (image != null) {
            this.width = width.get(0);
            this.height = height.get(0);

            if (channels.get(0) == 3) {
                glTexImage2D(target, 0, GL_RGB, this.width, this.height,
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(target, 0, GL_RGBA, this.width, this.height,
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'.";
            }
        } else {
            assert false : "Error: (Texture) Could not load image '" + path + "'.";
        }

        stbi_image_free(image);
    }

    public Texture(String path, Configuration conf) {
        this.target = conf.getTarget();
        this.path = path;
        this.id = glGenTextures();
        glBindTexture(target, id);

        if (conf.hasParameters()) {
            for (Parameter param : conf.getParameters()) {
                glTexParameteri(target, param.getName(), param.getValue());
            }
        }

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(conf.isFlip());
        ByteBuffer image = stbi_load(path, width, height, channels, 0);

        if (image != null) {
            this.width = width.get(0);
            this.height = height.get(0);

            if (channels.get(0) == 3) {
                glTexImage2D(target, conf.getLevel(), GL_RGB, this.width, this.height,
                        conf.getBorder(), GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(target, 0, GL_RGBA, this.width, this.height,
                        conf.getBorder(), GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'.";
            }
        } else {
            assert false : "Error: (Texture) Could not load image '" + path + "'.";
        }

        stbi_image_free(image);
    }

    public Texture(List<Image> images, Configuration conf) {
        this.target = GL_TEXTURE_2D_ARRAY;
        this.path = "generated";
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, id);

        if (conf.hasParameters()) {
            for (Parameter param : conf.getParameters()) {
                glTexParameteri(GL_TEXTURE_2D_ARRAY, param.getName(), param.getValue());
            }
        }

        Image image;
        if (!images.isEmpty() && ((image = images.get(0)) != null)) {
            this.width     = image.getWidth();
            this.height    = image.getHeight();
            int imageCount = images.size();
            int format     = (image.getChannels() == 4) ? GL_RGBA : GL_RGB;
            int type       = GL_UNSIGNED_BYTE;
            glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, format, width, height, imageCount,
                    0, format, type, 0);

            for (int i = 0; i < imageCount; i++) {
                image = images.get(i);
                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                        0, 0, i, width, height, 1,
                        format, type, image.getData());
                image.free();
            }

            glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        } else {
            assert false : "Error: (Texture) No images available.";
        }
    }

    public Texture(BufferedImage image) {
        this.target = GL_TEXTURE_2D;
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
        glBindTexture(target, id);
        glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexImage2D(target, 0, GL_RGBA8, width, height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        buffer.clear();
    }

    public Texture(int width, int height) {
        this.target = GL_TEXTURE_2D;
        this.width = width;
        this.height = height;
        this.path = "Generated";
        this.id = glGenTextures();
        glBindTexture(target, id);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(target, 0, GL_RGB, width, height,
                0, GL_RGB, GL_UNSIGNED_BYTE, 0);
    }

    public Texture(Configuration conf) {
        this.target = conf.getTarget();
        this.width = conf.getWidth();
        this.height = conf.getHeight();
        this.path = "Generated";
        this.id = glGenTextures();
        glBindTexture(target, id);
        if (conf.hasParameters()) {
            for (Parameter param : conf.getParameters()) {
                glTexParameteri(target, param.getName(), param.getValue());
            }
        }
        glTexImage2D(target, conf.getLevel(), conf.getInternalFormat(), width, height,
                conf.getBorder(), conf.getFormat(), conf.getType(), 0);
    }



    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void bind() {
        glBindTexture(target, id);
    }

    public void unbind() {
        glBindTexture(target, 0);
    }

    public int getId() {
        return id;
    }

    public int getTarget() {
        return target;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
