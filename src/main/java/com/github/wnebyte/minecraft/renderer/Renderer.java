package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.components.BoxRenderer;
import com.github.wnebyte.minecraft.util.JMath;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static Renderer getInstance() {
        if (Renderer.instance != null) {
            return Renderer.instance;
        } else {
            throw new IllegalStateException(
                    "Renderer has not yet been initialized."
            );
        }
    }

    public static void initialize(Camera camera) {
        if (Renderer.instance == null) {
            Renderer.instance = new Renderer(camera);
        } else {
            throw new IllegalStateException(
                    "Renderer has already been initialized."
            );
        }
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static Renderer instance;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final Camera camera;

    private final List<Batch<BoxRenderer>> boxBatches;

    private final List<Batch<Line2D>> line2DBatches;

    private final List<Batch<Line3D>> line3DBatches;

    private final List<Vertex2DBatchRenderer> vertex2DBatches;

    private final List<Vertex2DBatchRenderer> vertex2DTextBatches;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private Renderer(Camera camera) {
        this.camera = camera;
        this.boxBatches = new ArrayList<>();
        this.line2DBatches = new ArrayList<>();
        this.line3DBatches = new ArrayList<>();
        this.vertex2DBatches = new ArrayList<>();
        this.vertex2DTextBatches = new ArrayList<>();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void add(GameObject go) {
        BoxRenderer c = go.getComponent(BoxRenderer.class);
        if (c != null) {
            add(c);
        }
    }

    public void add(BoxRenderer box) {
        boolean added = false;
        for (Batch<BoxRenderer> batch : boxBatches) {
            if (batch.add(box)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<BoxRenderer> batch = new BoxBatchRenderer(camera, 100);
            batch.start();
            batch.add(box);
            boxBatches.add(batch);
        }
    }

    public void addLine3D(Line3D line) {
        boolean added = false;
        for (Batch<Line3D> batch : line3DBatches) {
            if (batch.add(line)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<Line3D> batch = new Line3DBatchRenderer(camera);
            batch.start();
            batch.add(line);
            line3DBatches.add(batch);
        }
    }

    public void addLine3D(Vector3f start, Vector3f end, Vector3f color) {
        addLine3D(start, end, color, 60 * 5);
    }

    public void addLine3D(Vector3f start, Vector3f end, Vector3f color, int ftl) {
        addLine3D(new Line3D(start, end, color, ftl));
    }

    public void addBox3D(Vector3f center, Vector3f dimensions, float rotation, Vector3f color) {
        addBox3D(center, dimensions, rotation, color, 60 * 5);
    }

    public void addBox3D(Vector3f center, Vector3f dimensions, float rotation, Vector3f color, int ftl) {
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
        addLine3D(vertices[0], vertices[1], color, ftl);
        addLine3D(vertices[1], vertices[3], color, ftl);
        addLine3D(vertices[3], vertices[2], color, ftl);
        addLine3D(vertices[2], vertices[0], color, ftl);
        // BACK
        addLine3D(vertices[4], vertices[5], color, ftl);
        addLine3D(vertices[5], vertices[7], color, ftl);
        addLine3D(vertices[7], vertices[6], color, ftl);
        addLine3D(vertices[6], vertices[4], color, ftl);
        // LEFT
        addLine3D(vertices[0], vertices[2], color, ftl);
        addLine3D(vertices[2], vertices[6], color, ftl);
        addLine3D(vertices[6], vertices[4], color, ftl);
        addLine3D(vertices[4], vertices[0], color, ftl);
        // RIGHT
        addLine3D(vertices[1], vertices[5], color, ftl);
        addLine3D(vertices[5], vertices[7], color, ftl);
        addLine3D(vertices[7], vertices[3], color, ftl);
        addLine3D(vertices[3], vertices[1], color, ftl);
        // TOP
        addLine3D(vertices[0], vertices[4], color, ftl);
        addLine3D(vertices[4], vertices[5], color, ftl);
        addLine3D(vertices[5], vertices[1], color, ftl);
        addLine3D(vertices[1], vertices[0], color, ftl);
        // BOTTOM
        addLine3D(vertices[2], vertices[6], color, ftl);
        addLine3D(vertices[6], vertices[7], color, ftl);
        addLine3D(vertices[7], vertices[3], color, ftl);
        addLine3D(vertices[3], vertices[2], color, ftl);
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

    public void addBox3DTop(Vector3f center, Vector3f dimensions, float rotation, Vector3f color, int ftl) {
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

        addLine3D(vertices[0], vertices[4], color, ftl);
        addLine3D(vertices[1], vertices[5], color, ftl);
        addLine3D(vertices[4], vertices[5], color, ftl);
        addLine3D(vertices[1], vertices[0], color, ftl);
    }

    public void addLine2D(Line2D line) {
        boolean added = false;
        for (Batch<Line2D> batch : line2DBatches) {
            if (batch.add(line)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<Line2D> batch = new Line2DBatchRenderer(camera);
            batch.start();
            batch.add(line);
            line2DBatches.add(batch);
        }
    }

    public void addLine2D(Vector2f start, Vector2f end, Vector3f color) {
        addLine2D(start, end, color, 60 * 5);
    }

    public void addLine2D(Vector2f start, Vector2f end, Vector3f color, int ftl) {
        addLine2D(new Line2D(start, end, color, ftl));
    }

    public void addBox2D(Vector2f center, Vector2f dimensions, Vector3f color, float rotation) {
        addBox2D(center, dimensions, color, rotation, 60 * 5);
    }

    public void addBox2D(Vector2f center, Vector2f dimensions, Vector3f color, float rotation, int ftl) {
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

        addLine2D(vertices[0], vertices[1], color, ftl);
        addLine2D(vertices[0], vertices[3], color, ftl);
        addLine2D(vertices[1], vertices[2], color, ftl);
        addLine2D(vertices[2], vertices[3], color, ftl);
    }

    public void drawText2D(Text2D text2D) {
        boolean added = false;
        for (Vertex2DBatchRenderer batch : vertex2DTextBatches) {
            if (batch.addText2D(text2D)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Vertex2DBatchRenderer batch = new Vertex2DBatchRenderer(camera);
            batch.start();
            batch.addText2D(text2D);
            vertex2DTextBatches.add(batch);
        }
    }

    public void drawQuad2D(float x, float y, float width, float height, float scale, int rgb) {
        boolean added = false;
        for (Vertex2DBatchRenderer batch : vertex2DBatches) {
            if (batch.addQuad(x, y, width, height, scale, null, null, rgb)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Vertex2DBatchRenderer batch = new Vertex2DBatchRenderer(camera);
            batch.start();
            batch.addQuad(x, y, width, height, scale, null, null, rgb);
            vertex2DBatches.add(batch);
        }
    }

    public void drawTexturedQuad2D(float x, float y, float width, float height, float scale, Sprite sprite, int rgb) {
        boolean added = false;
        for (Vertex2DBatchRenderer batch : vertex2DBatches) {
            if (batch.addQuad(x, y, width, height, scale, sprite.getTexture(), sprite.getTexCoords(), rgb)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Vertex2DBatchRenderer batch = new Vertex2DBatchRenderer(camera);
            batch.start();
            batch.addQuad(x, y, width, height, scale, sprite.getTexture(), sprite.getTexCoords(), rgb);
            vertex2DBatches.add(batch);
        }
    }

    public void render() {
        // set render states
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // 2D blendable
        for (int i = 0; i < vertex2DTextBatches.size(); i++) {
            Vertex2DBatchRenderer batch = vertex2DTextBatches.get(i);
            batch.flush();
        }

        glDisable(GL_BLEND);

        // 2D
        for (int i = 0; i < vertex2DBatches.size(); i++) {
            Vertex2DBatchRenderer batch = vertex2DBatches.get(i);
            batch.flush();
        }
        for (int i = 0; i < line2DBatches.size(); i++) {
            Batch<Line2D> batch = line2DBatches.get(i);
            batch.render();
        }

        // 3D
        for (int i = 0; i < line3DBatches.size(); i++) {
            Batch<Line3D> batch = line3DBatches.get(i);
            batch.render();
        }
        for (int i = 0; i < boxBatches.size(); i++) {
            Batch<BoxRenderer> batch = boxBatches.get(i);
            batch.render();
        }

    }

    public void destroy() {
        for (int i = 0; i < line2DBatches.size(); i++) {
            Batch<Line2D> batch = line2DBatches.get(i);
            batch.destroy();
        }
        for (int i = 0; i < vertex2DBatches.size(); i++) {
            Vertex2DBatchRenderer batch = vertex2DBatches.get(i);
            batch.destroy();
        }
        for (int i = 0; i < line3DBatches.size(); i++) {
            Batch<Line3D> batch = line3DBatches.get(i);
            batch.destroy();
        }
        for (int i = 0; i < boxBatches.size(); i++) {
            Batch<BoxRenderer> batch = boxBatches.get(i);
            batch.destroy();
        }
    }

    public void clearLines3D() {
        line3DBatches.clear();
    }
}
