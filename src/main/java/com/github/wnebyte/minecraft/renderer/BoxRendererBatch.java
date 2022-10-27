package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.BoxRenderer;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class BoxRendererBatch implements Batch<BoxRenderer> {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static Vector4f[] toVector4fArray(Transform transform) {
        Vector4f[] array = new Vector4f[8];
        Matrix4f transformMatrix = null;
        boolean isRotated = (transform.rotation != 0.0f);
        if (isRotated) {
            transformMatrix = transform.toMat4f();
        }

        for (int i = 0; i < array.length; i++) {
            float xAdd = VERTICES[i][0];
            float yAdd = VERTICES[i][1];
            float zAdd = VERTICES[i][2];
            Vector4f pos = new Vector4f(
                    transform.position.x + (xAdd * transform.scale.x),
                    transform.position.y + (yAdd * transform.scale.y),
                    transform.position.z + (zAdd * transform.scale.z),
                    1
            );
            if (isRotated) {
                pos = new Vector4f(xAdd, yAdd, zAdd, 1.0f).mul(transformMatrix);
            }
            array[i] = pos;
        }

        return array;
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int POS_SIZE = 3;

    public static final int COLOR_SIZE = 4;

    public static final int POS_OFFSET = 0;

    public static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    public static final int STRIDE = POS_SIZE + COLOR_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int DEFAULT_MAX_BATCH_SIZE = 100;

    public static final float[][] VERTICES = {
            { -0.5f,  0.5f,  0.5f },
            {  0.5f,  0.5f,  0.5f } ,
            { -0.5f, -0.5f,  0.5f },
            {  0.5f, -0.5f,  0.5f },
            { -0.5f,  0.5f, -0.5f },
            {  0.5f,  0.5f, -0.5f },
            { -0.5f, -0.5f, -0.5f },
            {  0.5f, -0.5f, -0.5f }
    };

    public static final int[] INDICES = {
            1, 0, 2, 3, 1, 2,
            5, 1, 3, 7, 5, 3,
            7, 6, 4, 5, 7, 4,
            0, 4, 6, 2, 0, 6,
            5, 4, 0, 1, 5, 0,
            3, 2, 6, 7, 3, 6
    };

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private int vaoID;

    private int vboID;

    private Camera camera;

    private Shader shader;

    private BoxRenderer[] boxes;

    private float[] data;

    private int maxBatchSize;

    private int size;

    private boolean started;

    private boolean destroyed;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public BoxRendererBatch(Camera camera) {
        this(camera, DEFAULT_MAX_BATCH_SIZE);
    }

    public BoxRendererBatch(Camera camera, int maxBatchSize) {
        this.camera = camera;
        this.maxBatchSize = maxBatchSize;
        this.boxes = new BoxRenderer[maxBatchSize];
        this.data = new float[maxBatchSize * STRIDE * 36];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/box.glsl");
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    @Override
    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long)data.length * STRIDE_BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        started = true;
    }

    @Override
    public void render() {
        if (!started) {
            start();
        }
        if (isDirty()) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, data);
        }
        shader.use();
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36 * size);
        glBindVertexArray(0);

        shader.detach();
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
        destroyed = true;
    }

    /**
     * Adds the properties of the specified <code>element</code> to this batch's underlying vertex dataset if
     * there is room.
     * @param element the element to be added.
     * @return <code>true</code> if the specfied element was successfully added,
     * otherwise <code>false</code>.
     */
    @Override
    public boolean add(BoxRenderer element) {
        if (size >= maxBatchSize) return false;
        int index = size;
        boxes[index] = element;
        size++;
        loadVertexProperties(index);
        return true;
    }

    /**
     * Removes the properties of the specified <code>element</code> from this batch's underlying vertex dataset.
     * @param element the element to be removed.
     * @return <code>true</code> if the specified element was successfully removed,
     * other <code>false</code>.
     */
    @Override
    public boolean remove(BoxRenderer element) {
        for (int i = 0; i < size; i++) {
            BoxRenderer b = boxes[i];
            if (b.equals(element)) {
                for (int j = i; j < size - 1; j++) {
                    boxes[j] = boxes[j + 1];
                    boxes[j].setDirty();
                }
                size--;
                return true;
            }
        }
        return false;
    }

    private boolean isDirty() {
        boolean rebuffer = false;
        for (int i = 0; i < size; i++) {
            BoxRenderer b = boxes[i];
            if (b.isDirty()) {
                loadVertexProperties(i);
                b.setClean();
                rebuffer = true;
            }
        }
        return rebuffer;
    }

    private void loadVertexProperties(int index) {
        BoxRenderer b = boxes[index];
        Vector4f[] verts = toVector4fArray(b.gameObject.transform);
        Vector4f color = b.getColor();
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < 36; i++) {
            index = INDICES[i];
            Vector4f pos = verts[index];
            data[offset + 0] = pos.x;
            data[offset + 1] = pos.y;
            data[offset + 2] = pos.z;
            data[offset + 3] = color.x;
            data[offset + 4] = color.y;
            data[offset + 5] = color.z;
            data[offset + 6] = color.w;
            offset += STRIDE;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int size() {
        return size;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    @Override
    public boolean hasSpace() {
        return (size < maxBatchSize);
    }

    @Override
    public void clear() {
        Arrays.fill(data, 0f);
        Arrays.fill(boxes, null);
        size = 0;
    }
}
