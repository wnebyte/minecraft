package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class Framebuffer {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static class Configuration {

        private int width;

        private int height;

        private List<Texture> colorAttachments;

        private Texture depthAttachment;

        private Configuration() {}

        private Configuration(int width, int height, List<Texture> colorAttachments, Texture depthAttachment) {
            this.width = width;
            this.height = height;
            this.colorAttachments = colorAttachments;
            this.depthAttachment = depthAttachment;
        }

        public boolean hasColorAttachments() {
            return (colorAttachments != null) && !(colorAttachments.isEmpty());
        }

        public boolean hasDepthAttachment() {
            return (depthAttachment != null);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public List<Texture> getColorAttachments() {
            return colorAttachments;
        }

        public Texture getDepthAttachment() {
            return depthAttachment;
        }

        public static class Builder {

            private int width, height;

            private List<Texture> colorAttachments = new ArrayList<>();

            private Texture depthAttachment;

            public Builder setWidth(int width) {
                this.width = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.height = height;
                return this;
            }

            public Builder setSize(int width, int height) {
                this.width = width;
                this.height = height;
                return this;
            }

            public Builder setColorAttachments(List<Texture> colorAttachments) {
                this.colorAttachments = colorAttachments;
                return this;
            }

            public Builder addColorAttachment(Texture colorAttachment) {
                this.colorAttachments.add(colorAttachment);
                return this;
            }

            public Builder setDepthAttachment(Texture depthAttachment) {
                this.depthAttachment = depthAttachment;
                return this;
            }

            public Configuration build() {
                Configuration conf = new Configuration();
                conf.width = width;
                conf.height = height;
                conf.colorAttachments = colorAttachments;
                conf.depthAttachment = depthAttachment;
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

    private int width, height;

    private List<Texture> colorAttachments;

    private Texture depthAttachment;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Framebuffer(Configuration conf) {
        this.id = glGenFramebuffers();
        this.width = conf.getWidth();
        this.height = conf.getHeight();
        this.colorAttachments = conf.getColorAttachments();
        this.depthAttachment = conf.getDepthAttachment();
        glBindFramebuffer(GL_FRAMEBUFFER, id);

        if (colorAttachments != null) {
            int i = 0;
            for (Texture colorAttachment : colorAttachments) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i,
                        colorAttachment.getTarget(),  colorAttachment.getId(), 0);
                i++;
            }
        }
        if (depthAttachment != null) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    depthAttachment.getTarget(), depthAttachment.getId(), 0);
        }

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE
                : "Error: (Framebuffer) Framebuffer is not complete.";

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public ByteBuffer readAllPixels(int index) {
        assert (index < colorAttachments.size()) : "Index is out of bounds";
        Texture texture = colorAttachments.get(index);
        int width = texture.getWidth();
        int height = texture.getHeight();
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        glBindFramebuffer(GL_FRAMEBUFFER, id);
        glReadBuffer(GL_COLOR_ATTACHMENT0 + index);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, pixels);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixIndex = (x + (y * width)) * 4;
                int outIndex = (x + ((height - y - 1) * width)) * 4;
                buffer.put(outIndex + 0, pixels.get(pixIndex + 3));
                buffer.put(outIndex + 1, pixels.get(pixIndex + 2));
                buffer.put(outIndex + 2, pixels.get(pixIndex + 1));
                buffer.put(outIndex + 3, pixels.get(pixIndex + 0));
            }
        }

        return buffer;
    }

    // Todo: impl
    public void resize(int width, int height) {

    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture getColorAttachment(int index) {
        return colorAttachments.get(index);
    }

    public Texture getDepthAttachment() {
        return depthAttachment;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Framebuffer)) return false;
        Framebuffer framebuffer = (Framebuffer) o;
        return Objects.equals(framebuffer.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 39;
        return result +
                3 +
                Objects.hashCode(this.id);
    }
}
