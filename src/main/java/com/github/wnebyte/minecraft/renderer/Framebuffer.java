package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class Framebuffer {

    public static class Configuration {

        private int width;

        private int height;

        private List<Texture> colorAttachments;

        private Texture depthAttachment;

        private Configuration() {

        }

        public boolean hasColorAttachment() {
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
                Configuration spec = new Configuration();
                spec.width = width;
                spec.height = height;
                spec.colorAttachments = colorAttachments;
                spec.depthAttachment = depthAttachment;
                return spec;
            }
        }
    }

    private int id;

    private List<Texture> colorAttachments;

    private Texture depthAttachment;

    public Framebuffer(Configuration spec) {
        this.id = glGenFramebuffers();
        this.colorAttachments = spec.getColorAttachments();
        this.depthAttachment = spec.getDepthAttachment();
        glBindFramebuffer(GL_FRAMEBUFFER, id);

        if (spec.hasColorAttachment()) {
            int i = 0;
            for (Texture colorAttachment : colorAttachments) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i,
                        colorAttachment.getTarget(),  colorAttachment.getId(), 0);
                i++;
            }
        }
        if (spec.hasDepthAttachment()) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    depthAttachment.getTarget(), depthAttachment.getId(), 0);
        }

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE
                : "Error: (Framebuffer) Framebuffer is not complete.";

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void resize(int width, int height) {

    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void drawColorBuffers() {
        int[] bufs = new int[colorAttachments.size()];
        for (int i = 0; i < bufs.length; i++) {
            bufs[i] = GL_COLOR_ATTACHMENT0 + i;
        }
        glDrawBuffers(bufs);
    }

    public int getId() {
        return id;
    }

    public Texture getColorAttachment(int index) {
        return colorAttachments.get(index);
    }

    public Texture getDepthAttachment() {
        return depthAttachment;
    }
}
