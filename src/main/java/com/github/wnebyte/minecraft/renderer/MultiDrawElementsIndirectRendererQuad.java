package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL42.glTexStorage3D;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

public class MultiDrawElementsIndirectRendererQuad {

    private static final int POS_SIZE = 2;

    private static final int UV_SIZE = 2;

    private static final int POS_OFFSET = 0;

    private static final int UV_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + UV_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int NUM_COMMANDS = 100;

    /*
     struct SDrawElementsCommand
  {
    GLuint vertexCount;
    GLuint instanceCount;
    GLuint firstIndex;
    GLuint baseVertex;
    GLuint baseInstance;
  };
     */

    private static final float[][] gQuad = {
            { 0.0f, 0.0f, 0.0f, 0.0f, },
            { 0.1f, 0.0f, 1.0f, 0.0f, },
            { 0.0f, 0.1f, 0.0f, 1.0f, },
            { 0.1f, 0.1f, 1.0f, 1.0f, }
    };

    private static final int[] gIndex = {
            0,1,2,
            1,3,2
    };

    private Shader shader;

    private List<Texture> textures;

    public MultiDrawElementsIndirectRendererQuad() {
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/quad.glsl");
        this.textures = Arrays.asList(
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/birch_log.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/dirt.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/birch_log_top.png"));
    }

    public void start() {
       // glUniform1i(0, 0);
        generateGeometry();
        //generateArrayTexture();
    }

    private void generateGeometry() {
        // generate 100 quads
        float[] vVertex = new float[100 * 4 * STRIDE];
        int index = 0;
        float xOffset = -0.95f;
        float yOffset = -0.95f;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 4; k++) {
                    vVertex[index]     = gQuad[k][0] + xOffset;
                    vVertex[index + 1] = gQuad[k][1] + yOffset;
                    vVertex[index + 2] = gQuad[k][2];
                    vVertex[index + 3] = gQuad[k][3];
                    index+=STRIDE;
                }
                xOffset += 0.2f;
            }
            yOffset += 0.2f;
            xOffset = -0.95f;
        }

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create a vertex buffer object
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vVertex, GL_STATIC_DRAW);

        // Create an element buffer
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, gIndex, GL_STATIC_DRAW);

        // Specify vertex attributes for the shader
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        // Generate draw commands
        index = 0;
        int[] vDrawCommands = new int[5 * NUM_COMMANDS];
        for (int i = 0; i < NUM_COMMANDS; i++) {
            vDrawCommands[index]     = 6;     // vertexCount
            vDrawCommands[index + 1] = 1;     // instanceCount (how many copies of the geometry we should draw)
            vDrawCommands[index + 2] = 0;     // firstIndex
            vDrawCommands[index + 3] = i * 4; // baseVertex (unique to drawElements)
            vDrawCommands[index + 4] = i;     // baseInstance
            index += 5;
        }

        int ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, vDrawCommands, GL_STATIC_DRAW);

        // Generate an instanced vertex array to identify each draw call/instance in the shader
        int[] vDrawId = new int[NUM_COMMANDS];
        for (int i = 0; i < NUM_COMMANDS; i++) {
            vDrawId[i] = i % textures.size();
        }

        int idbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, idbo);
        glBufferData(GL_ARRAY_BUFFER, vDrawId, GL_STATIC_DRAW);

        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, 0, 0);
        glVertexAttribDivisor(2, 1);
        glEnableVertexAttribArray(2);

        /*
        glBindBuffer(GL_ARRAY_BUFFER, ibo);
        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, 0, 5 * Integer.BYTES);
        glVertexAttribDivisor(2, 1);
         */
    }

    private void generateArrayTexture() {
        // Generate an array texture
        int id = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, id);

        // Create a storage for the texture. (100 layers of 1x1 texels)
        glTexStorage3D(GL_TEXTURE_2D_ARRAY,
                1,
                GL_RGB8,
                1, 1,
                100
        );

        int i = 0;
        for (ByteBuffer pixels : generateByteBuffers(NUM_COMMANDS)) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY,
                    0,
                    0, 0, i++,
                    1, 1, 1,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    pixels
            );
            pixels.clear();
        }

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void render() {
        shader.use();

        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
        }
        shader.uploadIntArray(Shader.U_TEXTURES, new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 });

        // param mode specifies what kind of primitive to render
        // param indirect is either an offset, in bytes, into the buffer bound to
        // GL_DRAW_INDIRECT_BUFFER or a pointer to an array struct that holds draw parameters
        // param drawCount the number of elements in the array addresses by indirect
        // param stride is the distance, in bytes, between the elements of the indirect array
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, NUM_COMMANDS, 0);

        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            texture.unbind();
        }

        shader.detach();
    }

    private int rand() {
        Random random = new Random();
        return random.nextInt();
    }

    private ByteBuffer[] generateByteBuffers(int size) {
        ByteBuffer[] buffers = new ByteBuffer[size];
        for (int i = 0; i < size; i++) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(3);
            buffer.put((byte)(i * 2));
            buffer.put((byte)i);
            buffer.put((byte)i);
            buffers[i] = buffer;
        }
        return buffers;
    }
}
