package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Line3DBatch implements Batch<Line3D> {

    private static final int MAX_LINES = 3000;

    private static final int POS_SIZE = 3;

    private static final int COLOR_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private int vboID;

    private int vaoID;

    private List<Line3D> lines;

    private float[] vertexArray;

    private Camera camera;

    private Shader shader;

    private boolean started;

    public Line3DBatch(Camera camera) {
        this.camera = camera;
        this.lines = new ArrayList<>(MAX_LINES);
        this.vertexArray = new float[MAX_LINES * 6 * 2];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/line3D.glsl");
    }

    @Override
    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long)vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glLineWidth(2.0f);
        started = true;
    }

    private void beginFrame() {
        if (!started) {
            start();
        }
        for (int i = 0; i < lines.size(); i++) {
            Line3D line = lines.get(i);
            if (line.beginFrame() < 0) {
                lines.remove(i);
                i--;
            }
        }
    }

    @Override
    public void render() {
        if (lines.isEmpty()) {
            return;
        }

        beginFrame();
        int index = 0;
        for (Line3D line : lines) {
            for (int i = 0; i < 2; i++) {
                Vector3f pos = (i == 0) ? line.getStart() : line.getEnd();
                Vector3f color = line.getColor();

                vertexArray[index + 0] = pos.x;
                vertexArray[index + 1] = pos.y;
                vertexArray[index + 2] = pos.z;

                vertexArray[index + 3] = color.x;
                vertexArray[index + 4] = color.y;
                vertexArray[index + 5] = color.z;

                index += STRIDE;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);

        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());

        glBindVertexArray(vaoID);
        glDrawArrays(GL_LINES, 0, lines.size());
        glBindVertexArray(0);

        shader.detach();
    }

    @Override
    public void destroy() {
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
    }

    @Override
    public boolean add(Line3D element) {
        if (hasSpace()) {
            lines.add(element);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Line3D element) {
        return lines.remove(element);
    }

    @Override
    public boolean hasSpace() {
        return (lines.size() < MAX_LINES);
    }

    @Override
    public void clear() {
        lines.clear();
    }
}
