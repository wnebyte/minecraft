package com.github.wnebyte.minecraft.core;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.World;
import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.renderer.Text2D;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.util.*;
import static org.lwjgl.glfw.GLFW.*;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;

public class Scene {

    private Camera camera;

    private Renderer renderer;

    private World world;

    private float crosshairSize = 0.10f;

    private float crosshairHalfSize = crosshairSize / 2.0f;

    private float debounceTime = 0.75f;

    private float debounce = debounceTime;

    private List<Float> frames = new ArrayList<>();

    private float fps = 0.0f;

    private Spritesheet hudSpritesheet;

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
        Renderer.initialize(camera);
        this.renderer = Renderer.getInstance();
        this.world = new World(camera);
    }

    private void loadResources() {
        TexturePacker.pack(
                    Assets.DIR + "/images/blocks",
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/images/generated/packedTextures.png",
                false, 32, 32);
        BlockMap.load(
                Assets.DIR + "/config/textureFormat.json",
                 Assets.DIR + "/config/blockFormat.json");
        BlockMap.bufferTexCoords();
        TerrainGenerator.load(
                Assets.DIR + "/config/terrainNoise.json",
                (int)System.currentTimeMillis());
        Assets.loadSpritesheet(
                Assets.DIR + "/config/hudSprites.json");
        hudSpritesheet = Assets.getSpritesheet(Assets.DIR + "/images/spritesheets/hudSprites.png");
    }

    public void start() {
        loadResources();
        world.start(this);
    }

    public void update(float dt) {
        debounce -= dt;
        world.update(dt);

        // camera position label
        Vector3f origin = new Vector3f(camera.getPosition());
        Text2D text = new Text2D(
                String.format("%.0f, %.0f, %.0f", origin.x, origin.y, origin.z),
                -3.0f + 0.05f, 1.2f, 0.005f, 0x0000);
        renderer.drawText2D(text);

        // chunk coords label
        Vector2i v = Chunk.toChunkCoords(origin);
        Text2D stext = new Text2D(
                String.format("%d, %d", v.x, v.y),
                -3.0f + 0.05f, 1.1f, 0.005f, 0x0000);
        renderer.drawText2D(stext);

        // fps label
        frames.add(dt);
        renderer.drawText2D(new Text2D(
                String.format("%.1f", fps),
                -3.0f + 0.05f, 1.0f, 0.005f, 0x0000));

        if (debounce <= 0) {
            float sum = frames.stream().reduce(0.0f, Float::sum);
            fps = (1 / (sum / frames.size()));
            frames.clear();
            debounce = debounceTime;
        }

        // crosshair
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

        hud();
    }

    private Inventory inventory = new Inventory();

    private void hud() {
        Sprite selSprite = hudSpritesheet.getSprite(5);
        Sprite regSprite = hudSpritesheet.getSprite(6);
        float xStart = ((24 * 0.005f) * 10) / 2;
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            Sprite sprite = inventory.isSlotSelected(i) ? selSprite : regSprite;
            renderer.drawTexturedQuad2D(-xStart, -1.4f, 24, 24, 0.005f, sprite, 0xFFFF);
            xStart -= (24 * 0.005f);
        }
    }

    public void render() {
        world.render();
        renderer.render();
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
    }

    public Camera getCamera() {
        return camera;
    }
}
