package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;

import com.github.wnebyte.minecraft.mycomponents.MyBlock;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.componenets.Text2D;

public class Renderer {

    private static final int MAX_BATCH_SIZE = 1000;

    private Camera camera;

    private Frustrum frustrum;

    private List<Batch> batches;

    private List<Text2D> texts;

    private TextRenderBatch textRenderBatch;

    private boolean started;

    public Renderer(Camera camera, Frustrum frustrum) {
        this.camera = camera;
        this.frustrum = frustrum;
        this.batches = new ArrayList<>(100);
        this.texts = new ArrayList<>(100);
        this.textRenderBatch = new TextRenderBatch(camera);
    }

    public void addText2D(Text2D text2D) {
        if (!started) {
            textRenderBatch.start();
            started = true;
        }
        texts.add(text2D);
    }

    public void clearText2D() {
        textRenderBatch.flush();
        texts.clear();
    }

    public void add(MyBlock block) {
        boolean added = false;
        for (Batch batch : batches) {
            if (batch.hasSpace(block.getTextures())) {
                batch.add(block);
                added = true;
                break;
            }
        }

        if (!added) {
            Batch batch = new Batch(MAX_BATCH_SIZE, camera);
            batch.start();
            batch.add(block);
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
                textRenderBatch.addText2D(text);
            }
            textRenderBatch.flush();
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
        textRenderBatch.destroy();
    }

    public int size() {
        return batches.size();
    }

}
