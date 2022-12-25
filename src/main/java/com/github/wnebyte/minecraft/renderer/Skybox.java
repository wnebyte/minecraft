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

    private static final float[] VERTICES = {
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

    private float blend;

    private boolean started;

    private final Camera camera;

    private final Shader shader;

    private final Cubemap dayCubemap;

    private final Cubemap nightCubemap;

    public Skybox(Camera camera) {
        this(camera, new Cubemap(Cubemap.Type.DAY), new Cubemap(Cubemap.Type.NIGHT));
    }

    public Skybox(Camera camera, Cubemap dayCubemap, Cubemap nightCubemap) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/cubemap.glsl");
        this.dayCubemap = dayCubemap;
        this.nightCubemap = nightCubemap;
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        started = true;
    }

    public void render() {
        if (!started) {
            start();
        }
        glDepthMask(false);
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, new Matrix4f(new Matrix3f(camera.getViewMatrix())));
        glActiveTexture(GL_TEXTURE0);
        dayCubemap.bind();
        shader.uploadInt(Shader.U_DAY_CUBEMAP, 0);
        glActiveTexture(GL_TEXTURE1);
        nightCubemap.bind();
        shader.uploadInt(Shader.U_NIGHT_CUBEMAP, 1);
        shader.uploadFloat(Shader.U_BLEND, blend);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        dayCubemap.unbind();
        nightCubemap.unbind();
        shader.detach();
        glDepthMask(true);
    }

    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    public void setBlend(float value) {
        this.blend = value;
    }
}
