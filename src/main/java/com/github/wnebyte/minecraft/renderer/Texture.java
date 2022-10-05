package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.awt.image.BufferedImage;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

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

    public static class Specification {

        private int
        target,
        level,
        internalFormat,
        width,
        height,
        border,
        format,
        type;

        private List<Parameter> parameters;

        private Specification() {}

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

        public static class Builder {

            public static final int DEFAULT_TARGET = GL_TEXTURE_2D;

            public static final int DEFAULT_LEVEL = 0;

            public static final int DEFAULT_BORDER = 0;

            private int target = DEFAULT_TARGET;

            private int level = DEFAULT_LEVEL;

            private int internalFormat;

            private int width;

            private int height;

            private int border = DEFAULT_BORDER;

            private int format;

            private int type;

            private List<Parameter> parameters = new ArrayList<>();

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

            public Specification build() {
                Specification spec = new Specification();
                spec.target = target;
                spec.level = level;
                spec.internalFormat = internalFormat;
                spec.width = width;
                spec.height = height;
                spec.border = border;
                spec.format = format;
                spec.type = type;
                spec.parameters = parameters;
                return spec;
            }
        }
    }

    private int id;

    private int target;

    private String path;

    private int width;

    private int height;

    public Texture(String path, boolean pixelate) {
        this.target = GL_TEXTURE_2D;
        this.path = path;

        // Generate texture on GPU
        id = glGenTextures();
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
        stbi_set_flip_vertically_on_load(true);
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

    public Texture(Specification spec) {
        this.target = spec.target;
        this.width = spec.width;
        this.height = spec.height;
        this.path = "Generated";
        this.id = glGenTextures();
        glBindTexture(target, id);
        if (spec.hasParameters()) {
            for (Parameter param : spec.parameters) {
                glTexParameteri(target, param.getName(), param.getValue());
            }
        }
        glTexImage2D(target, spec.level, spec.internalFormat, width, height, spec.border, spec.format, spec.type, 0);
    }

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
