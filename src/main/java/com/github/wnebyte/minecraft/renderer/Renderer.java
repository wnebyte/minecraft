package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;

import com.github.wnebyte.minecraft.componenets.Cube;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.componenets.Text2D;

public class Renderer {

    private static final int MAX_BATCH_SIZE = 1000;

    private Camera camera;

    private Frustrum frustrum;

    private List<Batch> batches;

    private List<Text2D> texts;

    private TextRenderBatch textRenderer;

    private boolean started;

    public Renderer(Camera camera) {
        this.camera = camera;
        this.batches = new ArrayList<>(100);
        this.texts = new ArrayList<>(100);
        this.textRenderer = new TextRenderBatch(camera);
    }

    public void addText2D(Text2D text2D) {
        if (!started) {
            textRenderer.start();
            started = true;
        }
        texts.add(text2D);
    }

    public void clearText2D() {
        textRenderer.flush();
        texts.clear();
    }

    public void add(Cube cube) {
        boolean added = false;
        for (Batch batch : batches) {
            if (batch.hasSpace(cube.getTextures())) {
                batch.add(cube);
                added = true;
                break;
            }
        }

        if (!added) {
            Batch batch = new Batch(MAX_BATCH_SIZE, camera);
            batch.start();
            batch.add(cube);
            batches.add(batch);
        }
    }

    public void render() {
        for (int i = 0; i < batches.size(); i++) {
            Batch batch = batches.get(i);
            batch.render();
        }
        if (!texts.isEmpty()) {
            for (int i = 0; i < texts.size(); i++) {
                Text2D text = texts.get(i);
                textRenderer.addText2D(text);
            }
            textRenderer.flush();
        }
    }

    public void rebuffer() {
        for (int i = 0; i < batches.size(); i++) {
            Batch batch = batches.get(i);
            batch.rebuffer();
        }
    }

    public void destroy() {
        for (int i = 0; i < batches.size(); i++) {
            Batch batch = batches.get(i);
            batch.destroy();
        }
        textRenderer.destroy();
    }

    public int size() {
        return batches.size();
    }

}
