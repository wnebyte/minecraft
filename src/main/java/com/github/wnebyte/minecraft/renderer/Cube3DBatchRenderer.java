package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.CapacitySet;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

public class Cube3DBatchRenderer implements Batch<Cube3D> {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static Vector4f[] toVector4fArray(Transform transform) {
        Vector4f[] array = new Vector4f[8];
        Matrix4f transformMatrix = null;
        boolean isRotated = (transform.rotation.x != 0.0f);
        if (isRotated) {
            transformMatrix = transform.toMat4f();
        }

        for (int i = 0; i < array.length; i++) {
            float xAdd = VERTICES[i][0];
            float yAdd = VERTICES[i][1];
            float zAdd = VERTICES[i][2];
            Vector4f pos;
            if (isRotated) {
                pos = new Vector4f(xAdd, yAdd, zAdd, 1.0f).mul(transformMatrix);
            } else {
                pos = new Vector4f(
                        transform.position.x + (xAdd * transform.scale.x),
                        transform.position.y + (yAdd * transform.scale.y),
                        transform.position.z + (zAdd * transform.scale.z),
                        1);
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

    private static final int POS_SIZE = 3;

    private static final int COLOR_SIZE = 3;

    private static final int UV_SIZE = 2;

    private static final int TEX_ID_SIZE = 1;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int UV_OFFSET = COLOR_OFFSET + (COLOR_SIZE * Float.BYTES);

    private static final int TEX_ID_OFFSET = UV_OFFSET + (UV_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE + UV_SIZE + TEX_ID_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private static final int BATCH_SIZE = 100;

    private static final float[][] VERTICES = {
            { -0.5f,  0.5f,  0.5f },
            {  0.5f,  0.5f,  0.5f } ,
            { -0.5f, -0.5f,  0.5f },
            {  0.5f, -0.5f,  0.5f },
            { -0.5f,  0.5f, -0.5f },
            {  0.5f,  0.5f, -0.5f },
            { -0.5f, -0.5f, -0.5f },
            {  0.5f, -0.5f, -0.5f }
    };

    private static final float[][] UNIT_VERTICES = {
            { -1.0f,  1.0f,  1.0f },
            {  1.0f,  1.0f,  1.0f } ,
            { -1.0f, -1.0f,  1.0f },
            {  1.0f, -1.0f,  1.0f },
            { -1.0f,  1.0f, -1.0f },
            {  1.0f,  1.0f, -1.0f },
            { -1.0f, -1.0f, -1.0f },
            {  1.0f, -1.0f, -1.0f }
    };

    private static final int[] INDICES = {
            1, 0, 2, 3, 1, 2,
            5, 1, 3, 7, 5, 3,
            7, 6, 4, 5, 7, 4,
            0, 4, 6, 2, 0, 6,
            5, 4, 0, 1, 5, 0,
            3, 2, 6, 7, 3, 6
    };

    private int vaoID;

    private int vboID;

    private int size;

    private boolean started;

    private final float[] data;

    private final Shader shader;

    private final CapacitySet<Integer> textures;

    public Cube3DBatchRenderer() {
        this.data = new float[BATCH_SIZE * STRIDE];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/vertex3D.glsl");
        this.textures = new CapacitySet<>(TEX_SLOTS.length);
        this.size = 0;
    }

    @Override
    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, BATCH_SIZE * STRIDE_BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);
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
        shader.uploadMatrix4f(Shader.U_VIEW, viewMatrix);
        shader.uploadMatrix4f(Shader.U_PROJECTION, projectionMatrix);
        int i = 0;
        for (int texId : textures) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, texId);
            i++;
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, size * 36);
        glBindVertexArray(0);

        Arrays.fill(data, 0, size * STRIDE, 0.0f);
        size = 0;
        glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    @Override
    public void render(Camera camera) {
        render(camera.getViewMatrix(), camera.getProjectionMatrix());
    }

    @Override
    public boolean add(Cube3D cube) {
        if (atCapacity(cube)) {
            return false;
        }
        textures.addAll(Arrays.asList(
                cube.getSideSprite().getTexId(),
                cube.getTopSprite().getTexId(),
                cube.getBottomSprite().getTexId()));
        int index = size;
        loadVertexProperties(index, cube);
        size++;
        return true;
    }

    private void loadVertexProperties(int index, Cube3D cube) {
        Vector4f[] verts = toVector4fArray(cube.getTransform());
        Vector3f color = cube.getColor();
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < 36; i++) {
            index = INDICES[i];
            Vector4f pos = verts[index];
            Sprite sprite = sprite(i, cube);
            Vector2f uv = uv(i, sprite);
            data[offset + 0] = pos.x;
            data[offset + 1] = pos.y;
            data[offset + 2] = pos.z;
            data[offset + 3] = color.x;
            data[offset + 4] = color.y;
            data[offset + 5] = color.z;
            data[offset + 6] = uv.x;
            data[offset + 7] = uv.y;
            data[offset + 8] = textures.indexOf(sprite.getTexId());
            offset += STRIDE;
        }
    }

    private Sprite sprite(int index, Cube3D cube) {
        if (index < (6 * 4)) {
            return cube.getSideSprite();
        }
        else if (index >= (6 * 4) && index < (6 * 5)) {
            return cube.getTopSprite();
        }
        else {
            return cube.getBottomSprite();
        }
    }

    private Vector2f uv(int index, Sprite sprite) {
        int mod = (index % 6);
        return sprite.getTexCoords(mod);
    }

    private boolean atCapacity(Cube3D cube) {
        return (size >= BATCH_SIZE || atTexCapacity(cube));
    }

    // Todo: impl
    private boolean atTexCapacity(Cube3D cube) {
        return !textures.containsAll(Arrays.asList(
                cube.getSideSprite().getTexId(),
                cube.getTopSprite().getTexId(),
                cube.getBottomSprite().getTexId()));
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    @Override
    public int zIndex() {
        return 0;
    }
}
