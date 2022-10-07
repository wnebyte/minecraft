package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.componenets.BoxRenderer;

public class Renderer {

    private Camera camera;

    private List<Batch<BoxRenderer>> batches;

    public Renderer(Camera camera) {
        this.camera = camera;
        this.batches = new ArrayList<>();
    }

    public void add(GameObject go) {
        BoxRenderer c = go.getComponent(BoxRenderer.class);
        if (c != null) {
            add(c);
        }
    }

    private void add(BoxRenderer c) {
        boolean added = false;
        for (Batch<BoxRenderer> batch : batches) {
            if (batch.add(c)) {
                added = true;
                break;
            }
        }
        if (!added) {
            Batch<BoxRenderer> batch = new BoxRendererBatch(camera, 100);
            batch.start();
            batch.add(c);
            batches.add(batch);
        }
    }

    public void render() {
        for (int i = 0; i < batches.size(); i++) {
            Batch<BoxRenderer> batch = batches.get(i);
            batch.render();
        }
    }

    public void destroy() {
        for (int i = 0; i < batches.size(); i++) {
            Batch<BoxRenderer> batch = batches.get(i);
            batch.destroy();
        }
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
