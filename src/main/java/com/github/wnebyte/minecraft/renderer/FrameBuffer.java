package com.github.wnebyte.minecraft.renderer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class FrameBuffer {

    private int id;

    private Texture texture;

    public FrameBuffer(int width, int height) {
        // Generate framebuffer
        this.id = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, id);

        // Create the texture to render the data to, and attach it to our framebuffer
        this.texture = new Texture(width, height);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                this.texture.getId(), 0);

        // Create render buffer, store the depth info
        int rboID = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID);

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE
                : "Error: (FrameBuffer) FrameBuffer is not complete.";

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
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

    public int getTextureId() {
        return texture.getId();
    }
}
