package com.github.wnebyte.minecraft.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.World;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.componenets.Text2D;
import com.github.wnebyte.minecraft.util.JMath;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.BlockMap;
import com.github.wnebyte.minecraft.util.TexturePacker;

public class Scene {

    private Camera camera;

    private Frustrum frustrum;

    private Renderer renderer;

    private World world;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    public Scene(Camera camera) {
        this.camera = camera;
        this.frustrum = new Frustrum();
        this.renderer = new Renderer(camera);
        this.world = new World(camera, renderer);
    }

    private void loadResources() {
        TexturePacker.pack(
                Assets.DIR     + "/images/blocks",
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/images/generated/packedTextures.png",
                false, 32, 32);
        BlockMap.load(
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR  + "/config/blockFormat.json");
        BlockMap.bufferTexCoords();
    }

    public void start() {
        loadResources();
        world.start(this);
    }

    public void update(float dt) {
        world.update(dt);
        renderer.clearText2D();
        Vector3f origin = new Vector3f(camera.getPosition());
        String s = String.format("%.1f, %.1f, %.1f", origin.x, origin.y, origin.z);
        Text2D text = new Text2D(s, -3.0f + 0.05f, 1.2f, 0.005f, 0x0000);
        renderer.addText2D(text);

        float crosshairSize = 0.10f;
        float crosshairHalfSize = crosshairSize / 2.0f;
        renderer.addLine2D(
                new Vector2f(0.0f, -crosshairHalfSize),
                new Vector2f(0.0f, crosshairHalfSize),
                new Vector3f(0f, 0f, 0f),
                1);
        renderer.addLine2D(
                new Vector2f(-crosshairHalfSize, 0.0f),
                new Vector2f(crosshairHalfSize,  0.0f),
                new Vector3f(0f, 0f, 0f),
                1);
    }

    public void render() {
        world.render();
        renderer.render();
    }

    public void destroy() {
        world.destroy();
        renderer.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

    public Frustrum getFrustrum() {
        return frustrum;
    }
}
