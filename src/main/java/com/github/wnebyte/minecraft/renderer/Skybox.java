package com.github.wnebyte.minecraft.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Skybox {

    private static final int POS_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int STRIDE = POS_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private final float[] VERTICES = {
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,
             1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

             1.0f, -1.0f, -1.0f,
             1.0f, -1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f,  1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            -1.0f,  1.0f, -1.0f,
             1.0f,  1.0f, -1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
             1.0f, -1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
             1.0f, -1.0f,  1.0f
    };

    private int vaoID;

    private int vboID;

    private Shader shader;

    private Cubemap cubemap;

    private Camera camera;

    public Skybox(Camera camera) {
        this(camera, new Cubemap(Cubemap.Type.DAY));
    }

    public Skybox(Camera camera, Cubemap cubemap) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/cubemap.glsl");
        this.cubemap = cubemap;
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);
    }

    public void render() {
        glDepthMask(false);
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, new Matrix4f(new Matrix3f(camera.getViewMatrix())));
        glActiveTexture(GL_TEXTURE0);
        cubemap.bind();
        shader.uploadInt("skybox", 0);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        cubemap.unbind();
        shader.detach();
        glDepthMask(true);
    }

    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    public void setCubemap(Cubemap cubemap) {
        this.cubemap = cubemap;
    }
}
