package com.github.wnebyte.minecraft.ui;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.util.JColor;

public class InfoWindow implements JGuiWindow {

    private static final float TEXT_SCALE = 0.0045f;

    private static final int TEXT_COLOR = JColor.WHITE_HEX;

    private Camera camera;

    private List<Float> frames = new ArrayList<>();

    private float fps = 0.0f;

    private float debounceTime = 0.75f;

    private float debounce = debounceTime;

    public InfoWindow(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void update(float dt) {
        debounce -= dt;
        frames.add(dt);

        Vector3f pos = camera.getPosition();
        Vector2i cc = Chunk.toChunkCoords(pos);
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();

        JGui.begin(-3.0f, 1.3f, 0.5f, 0.3f, JColor.BLACK_HEX);
        JGui.label(String.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z), TEXT_SCALE, TEXT_COLOR);
        JGui.label(String.format("%d %d",            cc.x, cc.y),          TEXT_SCALE, TEXT_COLOR);
        JGui.label(String.format("%.1f",             fps),                 TEXT_SCALE, TEXT_COLOR);
       // JGui.label(String.format("%.2f",             pitch),               TEXT_SCALE, TEXT_COLOR);
       // JGui.label(String.format("%.2f",             yaw),                 TEXT_SCALE, TEXT_COLOR);
        JGui.end();

        if (debounce <= 0) {
            float sum = frames.stream().reduce(0.0f, Float::sum);
            fps = (1 / (sum / frames.size()));
            frames.clear();
            debounce = debounceTime;
        }
    }
}
