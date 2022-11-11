package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.components.BlockRenderer;

public class EntityRenderer {

    private List<EntityBatch<BlockRenderer>> batches;

    public EntityRenderer() {
        this.batches = new ArrayList<>();
    }

    public void add(GameObject go) {
        BlockRenderer br = go.getComponent(BlockRenderer.class);
        if (br != null) {
            add(br);
        }
    }

    private void add(BlockRenderer br) {

    }
}
