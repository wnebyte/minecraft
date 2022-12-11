package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Line2DBatchRenderer implements Batch<Line2D> {

    private static final int POS_SIZE = 2;

    private static final int COLOR_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int MAX_LINES = 3000;

    private int vaoID;

    private int vboID;

    private int size;

    private boolean started;

    private final float[] data;

    private final int zIndex;

    private final float width;

    private final Shader shader;

    public Line2DBatchRenderer(int zIndex, float width) {
        this.zIndex = zIndex;
        this.width = width;
        this.data = new float[MAX_LINES * 2 * STRIDE];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/line2D.glsl");
        this.size = 0;
    }

    @Override
    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, MAX_LINES * 2 * STRIDE_BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        started = true;
    }

    @Override
    public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        if (size <= 0) {
            return;
        }
        if (!started) {
            start();
        }
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);

        shader.use();
        shader.uploadInt(Shader.Z_INDEX, zIndex);
        shader.uploadMatrix4f(Shader.U_VIEW, viewMatrix);
        shader.uploadMatrix4f(Shader.U_PROJECTION, projectionMatrix);

        glLineWidth(width);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_LINES, 0, size * 2);
        glBindVertexArray(0);

        // Reset batch for use on the next draw call
        Arrays.fill(data, 0, size * 2 * STRIDE, 0.0f);
        size = 0;
        shader.detach();
    }

    @Override
    public void render(Camera camera) {
        render(camera.getViewMatrixHUD(), camera.getProjectionMatrixHUD());
    }

    @Override
    public boolean add(Line2D line) {
        if (line.getZIndex() != zIndex || line.getWidth() != width || atCapacity()) {
            return false;
        }
        int index = size * 2 * STRIDE;
        loadVertexProperties(index, line);
        size++;
        return true;
    }

    private void loadVertexProperties(int index, Line2D line) {
        for (int i = 0; i < 2; i++) {
            Vector2f position = (i == 0) ? line.getStart() : line.getEnd();
            Vector3f color = line.getColor();
            data[index + 0] = position.x;
            data[index + 1] = position.y;
            data[index + 2] = color.x;
            data[index + 3] = color.y;
            data[index + 4] = color.z;
            index += STRIDE;
        }
    }

    public boolean atCapacity() {
        return (size >= MAX_LINES);
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    @Override
    public int zIndex() {
        return zIndex;
    }
}
