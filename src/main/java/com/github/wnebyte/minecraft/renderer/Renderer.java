package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.fonts.MFont;
import com.github.wnebyte.minecraft.fonts.CharInfo;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.JMath;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static Renderer getInstance() {
        if (Renderer.instance == null) {
            Renderer.instance = new Renderer();
        }
        return Renderer.instance;
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static Renderer instance;

    private static final Comparator<Batch<?>> COMPARATOR = Comparator.comparingInt(Batch::zIndex);

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final List<Batch<Line2D>> line2DBatches;

    private final List<Batch<Line3D>> line3DBatches;

    private final List<Batch<Vertex2D>> vertex2DBlendableBatches;

    private final List<Batch<Vertex2D>> vertex2DBatches;

    private final List<Batch<?>> batches;

    private final List<Batch<?>> blendableBatches;

    private final MFont font;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private Renderer() {
        this.line2DBatches = new ArrayList<>();
        this.line3DBatches = new ArrayList<>();
        this.vertex2DBatches = new ArrayList<>();
        this.vertex2DBlendableBatches = new ArrayList<>();
        this.batches = new ArrayList<>();
        this.blendableBatches = new ArrayList<>();
        this.font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void drawLine3D(Line3D line) {
        boolean added = false;
        for (Batch<Line3D> batch : line3DBatches) {
            if (batch.add(line)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<Line3D> batch = new Line3DBatchRenderer();
            batch.start();
            batch.add(line);
            line3DBatches.add(batch);
            addBatch(batch, false);
        }
    }

    public void drawLine3D(Vector3f start, Vector3f end, Vector3f color) {
        drawLine3D(new Line3D(start, end, color));
    }

    public void drawBox3D(Vector3f center, Vector3f dimensions, float rotation, Vector3f color) {
        Vector3f min = new Vector3f(center).sub(new Vector3f(dimensions).mul(0.5f));
        Vector3f max = new Vector3f(center).add(new Vector3f(dimensions).mul(0.5f));

        Vector3f[] vertices = {
                new Vector3f(min.x, max.y, max.z),
                new Vector3f(max.x, max.y, max.z),
                new Vector3f(min.x, min.y, max.z),
                new Vector3f(max.x, min.y, max.z),
                new Vector3f(min.x, max.y, min.z),
                new Vector3f(max.x, max.y, min.z),
                new Vector3f(min.x, min.y, min.z),
                new Vector3f(max.x, min.y, min.z)
        };

        if (rotation != 0.0f) {
            for (Vector3f vert : vertices) {
                JMath.rotate(vert, rotation, center);
            }
        }

        // FRONT
        drawLine3D(vertices[0], vertices[1], color);
        drawLine3D(vertices[1], vertices[3], color);
        drawLine3D(vertices[3], vertices[2], color);
        drawLine3D(vertices[2], vertices[0], color);
        // BACK
        drawLine3D(vertices[4], vertices[5], color);
        drawLine3D(vertices[5], vertices[7], color);
        drawLine3D(vertices[7], vertices[6], color);
        drawLine3D(vertices[6], vertices[4], color);
        // LEFT
        drawLine3D(vertices[0], vertices[2], color);
        drawLine3D(vertices[2], vertices[6], color);
        drawLine3D(vertices[6], vertices[4], color);
        drawLine3D(vertices[4], vertices[0], color);
        // RIGHT
        drawLine3D(vertices[1], vertices[5], color);
        drawLine3D(vertices[5], vertices[7], color);
        drawLine3D(vertices[7], vertices[3], color);
        drawLine3D(vertices[3], vertices[1], color);
        // TOP
        drawLine3D(vertices[0], vertices[4], color);
        drawLine3D(vertices[4], vertices[5], color);
        drawLine3D(vertices[5], vertices[1], color);
        drawLine3D(vertices[1], vertices[0], color);
        // BOTTOM
        drawLine3D(vertices[2], vertices[6], color);
        drawLine3D(vertices[6], vertices[7], color);
        drawLine3D(vertices[7], vertices[3], color);
        drawLine3D(vertices[3], vertices[2], color);
    }

    //   v4 ----------- v5
    //   /|            /|      Axis orientation
    //  / |           / |
    // v0 --------- v1  |      y
    // |  |         |   |      |
    // |  v6 -------|-- v7     +--- x
    // | /          |  /      /
    // |/           | /      z
    // v2 --------- v3

    public void drawLine2D(Line2D line) {
        boolean added = false;
        for (Batch<Line2D> batch : line2DBatches) {
            if (batch.add(line)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<Line2D> batch = new Line2DBatchRenderer(line.getZIndex());
            batch.start();
            batch.add(line);
            line2DBatches.add(batch);
            addBatch(batch, false);
        }
    }

    public void drawLine2D(Vector2f start, Vector2f end, int zIndex, Vector3f color) {
        drawLine2D(new Line2D(start, end, zIndex, color));
    }

    public void drawBox2D(Vector2f center, Vector2f dimensions, int zIndex, Vector3f color, float rotation) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).mul(0.5f));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).mul(0.5f));

        Vector2f[] vertices = {
                new Vector2f(min.x, min.y),
                new Vector2f(min.x, max.y),
                new Vector2f(max.x, max.y),
                new Vector2f(max.x, min.y)
        };

        if (rotation != 0.0f) {
            for (Vector2f vert : vertices) {
                JMath.rotate(vert, rotation, center);
            }
        }

        drawLine2D(vertices[0], vertices[1], zIndex, color);
        drawLine2D(vertices[0], vertices[3], zIndex, color);
        drawLine2D(vertices[1], vertices[2], zIndex, color);
        drawLine2D(vertices[2], vertices[3], zIndex, color);
    }

    public void drawVertex2D(Vertex2D vertex) {
        drawVertex2D(vertex, false);
    }

    public void drawVertex2D(Vertex2D vertex, boolean blendable) {
        List<Batch<Vertex2D>> vBatches = blendable ? vertex2DBlendableBatches : vertex2DBatches;
        boolean added = false;
        for (Batch<Vertex2D> batch : vBatches) {
            if (batch.add(vertex)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<Vertex2D> batch = new Vertex2DBatchRenderer(vertex.getZIndex());
            batch.start();
            batch.add(vertex);
            vBatches.add(batch);
            addBatch(batch, blendable);
        }
    }

    public void drawQuad2D(float x, float y, int z, int width, int height, float scale, int rgb) {
        // position
        float x0 = x;
        float y0 = y;
        float x1 = x + scale * width;
        float y1 = y + scale * height;
        // color
        float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float)((rgb >> 8)  & 0xFF) / 255.0f;
        float b = (float)((rgb >> 0)  & 0xFF) / 255.0f;

        Vertex2D[] vertices = {
                new Vertex2D(new Vector2f(x1, y0), z, new Vector3f(r, g, b), new Vector2f(), -1),
                new Vertex2D(new Vector2f(x1, y1), z, new Vector3f(r, g, b), new Vector2f(), -1),
                new Vertex2D(new Vector2f(x0, y1), z, new Vector3f(r, g, b), new Vector2f(), -1),
                new Vertex2D(new Vector2f(x0, y0), z, new Vector3f(r, g, b), new Vector2f(), -1)
        };

        for (Vertex2D vertex : vertices) {
            drawVertex2D(vertex);
        }
    }

    public void drawTexturedQuad2D(float x, float y, int z, Sprite sprite, float scale, int rgb) {
        // sprite
        float width = sprite.getWidth();
        float height = sprite.getHeight();
        Vector2f[] uvs = sprite.getTexCoords();
        int texId = sprite.getTexId();
        // position
        float x0 = x;
        float y0 = y;
        float x1 = x + scale * width;
        float y1 = y + scale * height;
        // color
        float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float)((rgb >> 8)  & 0xFF) / 255.0f;
        float b = (float)((rgb >> 0)  & 0xFF) / 255.0f;

        Vertex2D[] vertices = {
                new Vertex2D(new Vector2f(x1, y0), z, new Vector3f(r, g, b), new Vector2f(uvs[0].x, uvs[0].y), texId),
                new Vertex2D(new Vector2f(x1, y1), z, new Vector3f(r, g, b), new Vector2f(uvs[1].x, uvs[1].y), texId),
                new Vertex2D(new Vector2f(x0, y1), z, new Vector3f(r, g, b), new Vector2f(uvs[2].x, uvs[2].y), texId),
                new Vertex2D(new Vector2f(x0, y0), z, new Vector3f(r, g, b), new Vector2f(uvs[3].x, uvs[3].y), texId)
        };

        for (Vertex2D vertex : vertices) {
            drawVertex2D(vertex);
        }
    }

    public void drawString(String text, float x, float y, int z, float scale, int rgb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo info = font.getCharacter(c);
            if (info.getWidth() == 0) {
                System.err.printf("Warning: (Renderer) Unknown char: '%c'%n", c);
                continue;
            }

            // char info
            int width = info.getWidth();
            int height = info.getHeight();
            Vector2f[] uvs = info.getTexCoords();
            int texId = font.getTextureId();
            // position
            float x0 = x;
            float y0 = y;
            float x1 = x + scale * width;
            float y1 = y + scale * height;
            // color
            float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
            float g = (float)((rgb >> 8)  & 0xFF) / 255.0f;
            float b = (float)((rgb >> 0)  & 0xFF) / 255.0f;
            // tex coords
            float ux0 = uvs[0].x;
            float uy0 = uvs[0].y;
            float ux1 = uvs[1].x;
            float uy1 = uvs[1].y;

            Vertex2D[] vertices = {
                    new Vertex2D(new Vector2f(x1, y0), z, new Vector3f(r, g, b), new Vector2f(ux1, uy0), texId),
                    new Vertex2D(new Vector2f(x1, y1), z, new Vector3f(r, g, b), new Vector2f(ux1, uy1), texId),
                    new Vertex2D(new Vector2f(x0, y1), z, new Vector3f(r, g, b), new Vector2f(ux0, uy1), texId),
                    new Vertex2D(new Vector2f(x0, y0), z, new Vector3f(r, g, b), new Vector2f(ux0, uy0), texId)
            };

            for (Vertex2D vertex : vertices) {
                drawVertex2D(vertex, true);
            }

            x += info.getWidth() * scale;
        }
    }

    public void render(Camera camera) {
        // set render states
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // blendable
        blendableBatches.sort(COMPARATOR);
        for (int i = 0; i < blendableBatches.size(); i++) {
            Batch<?> batch = blendableBatches.get(i);
            batch.render(camera);
        }

        // set render states
        glDisable(GL_BLEND);

        // non-blendable
        batches.sort(COMPARATOR);
        for (int i = 0; i < batches.size(); i++) {
            Batch<?> batch = batches.get(i);
            batch.render(camera);
        }
    }

    public void destroy() {
        for (int i = 0; i < blendableBatches.size(); i++) {
            Batch<?> batch = blendableBatches.get(i);
            batch.destroy();
        }
        for (int i = 0; i < batches.size(); i++) {
            Batch<?> batch = batches.get(i);
            batch.destroy();
        }
    }

    private void addBatch(Batch<?> batch, boolean blendable) {
        if (blendable) {
            blendableBatches.add(batch);
        } else {
            batches.add(batch);
        }
    }
}
