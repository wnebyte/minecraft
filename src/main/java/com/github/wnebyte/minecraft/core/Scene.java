package com.github.wnebyte.minecraft.core;

import java.util.List;
import java.util.ArrayList;
import org.joml.*;
import com.github.wnebyte.minecraft.world.*;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.ui.Hud;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.util.*;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;

public class Scene {

    private Camera camera;

    private Renderer renderer;

    private World world;

    private float debounceTime = 0.75f;

    private float debounce = debounceTime;

    private List<Float> frames = new ArrayList<>();

    private float fps = 0.0f;

    private Hud hud;

    public Scene() {
        this.camera = new Camera(
                new Vector3f(0.0f, 0.0f, 3.0f),  // position
                new Vector3f(0.0f, 0.0f, -1.0f), // forward
                new Vector3f(0.0f, 1.0f, 0.0f),  // up
                Camera.DEFAULT_YAW,
                Camera.DEFAULT_PITCH,
                10f,
                Camera.DEFAULT_MOUSE_SENSITIVITY,
                Camera.DEFAULT_ZOOM,
                Camera.DEFAULT_Z_NEAR,
                10_000f);
        this.renderer = Renderer.getInstance();
        this.world = new World(camera);
        this.hud = new Hud(camera);
    }

    private void loadResources() {
        TexturePacker.pack(
                    Assets.DIR + "/images/blocks",
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/images/generated/packedTextures.png",
                false, 32, 32);
        BlockMap.loadBlocks(
                Assets.DIR + "/config/blockFormat.json",
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/images/generated/packedTextures.png");
        TexturePacker.pack(
                    Assets.DIR + "/images/items",
                Assets.DIR + "/config/itemTextureFormat.json",
                Assets.DIR + "/images/generated/packedItemTextures.png",
                false, 32, 32);
        BlockMap.loadItems(
                Assets.DIR + "/config/itemFormat.json",
                Assets.DIR + "/config/itemTextureFormat.json",
                Assets.DIR + "/images/generated/packedItemTextures.png");
        BlockMap.generateBlockItemImages(
                Assets.DIR + "/config/blockFormat.json",
                Assets.DIR + "/images/generated/blockItems");
        TexturePacker.pack(
                Assets.DIR     + "/images/generated/blockItems",
                Assets.DIR + "/config/blockItemTextureFormat.json",
                Assets.DIR + "/images/generated/packedBlockItemTextures.png",
                false, 32, 32);
        BlockMap.loadBlockItems(
                Assets.DIR + "/config/blockItemTextureFormat.json",
                Assets.DIR + "/images/generated/packedBlockItemTextures.png");
        BlockMap.bufferTexCoords();
        TerrainGenerator.load(
                Assets.DIR + "/config/terrainNoise.json",
                (int)System.currentTimeMillis());
        Assets.loadSpritesheet(
                Assets.DIR + "/config/hudSprites.json");
    }

    public void start() {
        loadResources();
        hud.start();
        world.start(this);
    }

    public void update(float dt) {
        debounce -= dt;
        world.update(dt);

        renderer.drawQuad2D(-3.0f + 0.005f, (1.2f - 0.3f) - 0.005f, -6, 1.3f, 0.4f, 0x0000);

        // camera position label
        Vector3f pos = new Vector3f(camera.getPosition());
        renderer.drawString(
                String.format("%.0f %.0f %.0f", pos.x, pos.y, pos.z),
                -3.0f + 0.05f, 1.2f, -5, 0.0045f, 0xFFFF);

        // chunk coords label
        Vector2i v = Chunk.toChunkCoords(pos);
        renderer.drawString(
                String.format("%d, %d", v.x, v.y),
                -3.0f + 0.05f, 1.1f, -5, 0.0045f, 0xFFFF);

        // fps label
        frames.add(dt);
        renderer.drawString(
                String.format("%.1f", fps),
                -3.0f + 0.05f, 1.0f, -5, 0.0045f, 0xFFFF);

        renderer.drawString(
                "ABCDEFGHIJKLMNOPQRSTUVYWX",
                -3.0f + 0.05f, 0.9f, -5, 0.0045f, 0xFFFF);

        if (debounce <= 0) {
            float sum = frames.stream().reduce(0.0f, Float::sum);
            fps = (1 / (sum / frames.size()));
            frames.clear();
            debounce = debounceTime;
        }

        hud.update(dt);
    }

    public void render() {
        world.render();
        renderer.flush(camera);
    }

    public void destroy() {
        world.destroy();
        renderer.destroy();
    }

    public void processInput(float dt) {
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setWindowShouldClose(true);
        }
        if (isKeyPressed(GLFW_KEY_W)) {
            camera.handleKeyboard(Camera.Movement.FORWARD, dt);
        }
        if (isKeyPressed(GLFW_KEY_S)) {
            camera.handleKeyboard(Camera.Movement.BACKWARD, dt);
        }
        if (isKeyPressed(GLFW_KEY_A)) {
            camera.handleKeyboard(Camera.Movement.LEFT, dt);
        }
        if (isKeyPressed(GLFW_KEY_D)) {
            camera.handleKeyboard(Camera.Movement.RIGHT, dt);
        }
        if (isKeyPressed(GLFW_KEY_SPACE)) {
            camera.handleKeyboard(Camera.Movement.UP, dt);
        }
        if (isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            camera.handleKeyboard(Camera.Movement.DOWN, dt);
        }
        if (isKeyPressed(GLFW_KEY_COMMA)) {
            camera.resetZoom();
        }
        if (isKeyBeginPress(GLFW_KEY_K)) {
            hud.showInventory(!hud.isInventoryShowing());
            if (hud.isInventoryShowing()) {
                float x = Application.getWindow().getWidth() / 2.0f;
                float y = Application.getWindow().getHeight() / 2.0f;
                Application.getWindow().setCursorPos(x, y);
            }
        }
        if (isKeyBeginPress(GLFW_KEY_RIGHT)) {
            Inventory.Hotbar hotbar = hud.getHotbar();
            hotbar.next();
        }
        if (isKeyBeginPress(GLFW_KEY_LEFT)) {
            Inventory.Hotbar hotbar = hud.getHotbar();
            hotbar.previous();
        }
    }

    public Camera getCamera() {
        return camera;
    }
}
