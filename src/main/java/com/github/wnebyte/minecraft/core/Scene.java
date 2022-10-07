package com.github.wnebyte.minecraft.core;

import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.world.World;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.BlockMap;
import com.github.wnebyte.minecraft.util.TexturePacker;

public class Scene {

    private Camera camera;

    private Frustrum frustrum;

    private Chunk chunk;

    private World world;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    public Scene(Camera camera) {
        this.camera = camera;
        this.frustrum = new Frustrum();
        this.world = new World(camera);
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
        world.start();
    }

    public void update(float dt) {
        world.update(dt);
        /*
        Vector3f v = camera.getPosition();
        String s = String.format("%.0f, %.0f, %.0f", v.x, v.y, v.z);
        Text2D text = new Text2D(s, -3.6f, 1.5f, 0.0070f, 0xFFFF);
        renderer.clearText2D();
        renderer.addText2D(text);
         */
    }

    public void render() {
        world.render();
    }

    public void destroy() {
        world.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

    public Frustrum getFrustrum() {
        return frustrum;
    }
}
