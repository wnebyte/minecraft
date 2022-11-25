package com.github.wnebyte.minecraft.ui;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.util.JColor;

public class InfoWindow implements JGuiWindow {

    private Camera camera;

    private List<Float> frames = new ArrayList<>();

    private float fps = 0.0f;

    private float debounceTime = 0.75f;

    private float debounce = debounceTime;

    public InfoWindow(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void jgui(float dt) {
        debounce -= dt;
        frames.add(dt);

        Vector3f pos = camera.getPosition();
        Vector2i cpos = Chunk.toChunkCoords(pos);

        JGui.begin(-3.0f, 1.3f, 0.5f, 0.3f, JColor.BLACK_HEX);
        JGui.label(String.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z), 0.0045f, JColor.WHITE_HEX);
        JGui.label(String.format("%d %d", cpos.x, cpos.y), 0.0045f, JColor.WHITE_HEX);
        JGui.label(String.format("%.1f", fps), 0.0045f, JColor.WHITE_HEX);
        JGui.end();

        if (debounce <= 0) {
            float sum = frames.stream().reduce(0.0f, Float::sum);
            fps = (1 / (sum / frames.size()));
            frames.clear();
            debounce = debounceTime;
        }
    }
}
