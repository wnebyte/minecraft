package com.github.wnebyte.minecraft.core;

import java.util.List;
import java.util.ArrayList;
import org.joml.*;
import com.github.wnebyte.minecraft.world.*;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.ui.Hud;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.event.EventSystem;
import com.github.wnebyte.minecraft.util.*;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Scene {

    private Camera camera;

    private Renderer renderer;

    private World world;

    private float debounceTime = 0.75f;

    private float debounce = debounceTime;

    private List<Float> frames = new ArrayList<>();

    private float fps = 0.0f;

    private Hud hud;

    private EventSystem eventSystem;

    public Scene() {
        this.camera = new Camera.Builder()
                .setPosition(0.0f, 0.0f, 3.0f)
                .setMovementSpeed(10f)
                .setZFar(10_000f)
                .build();
        this.renderer = Renderer.getInstance();
        this.eventSystem = new EventSystem();
    }

    protected void loadResources() {
        TexturePacker packer = new TexturePacker(true, true);

        // load blocks
        String path = Assets.DIR + "/images/generated/packedTextures.png";
        packer.pack(
                Assets.DIR + "/images/blocks",
                Assets.DIR + "/config/textureFormat.json",
                path, false, 32, 32);
        Texture texture = new Texture(path, new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .flip()
                .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                .build());
        Assets.addTexture(texture);
        BlockMap.loadBlocks(
                Assets.DIR + "/config/blockFormat.json",
                Assets.DIR + "/config/textureFormat.json",
                path);
        BlockMap.bufferTexCoords();

        // load items
        packer = new TexturePacker();
        path = Assets.DIR + "/images/generated/packedItemTextures.png";
        packer.pack(
                Assets.DIR + "/images/items",
                Assets.DIR + "/config/itemTextureFormat.json",
                path, false, 32, 32);
        texture = new Texture(path, new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                .build());
        Assets.addTexture(texture);
        BlockMap.loadItems(
                Assets.DIR + "/config/itemFormat.json",
                Assets.DIR + "/config/itemTextureFormat.json",
                path);

        // generate and load block items
        path = Assets.DIR + "/images/generated/packedBlockItemTextures.png";
        BlockMap.generateBlockItemImages(
                Assets.DIR + "/config/blockFormat.json",
                Assets.DIR + "/images/generated/blockItems");
        packer.pack(
                Assets.DIR + "/images/generated/blockItems",
                Assets.DIR + "/config/blockItemTextureFormat.json",
                path, false, 32, 32);
        texture = new Texture(path, new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                .build());
        Assets.addTexture(texture);
        BlockMap.loadBlockItems(
                Assets.DIR + "/config/blockItemTextureFormat.json",
                path);

        // load remaining assets
        TerrainGenerator.load(
                Assets.DIR + "/config/terrainNoise.json",
                (int)System.currentTimeMillis());
        Assets.loadSpritesheet(
                Assets.DIR + "/config/hudSprites.json");
    }

    public void start() {
        loadResources();
        world = new World(camera);
        hud = new Hud(camera);
        world.start(this);
        hud.start(this);
    }

    public void update(float dt) {
        debounce -= dt;
        world.update(dt);

        // draw overlay
        renderer.drawQuad2D(-3.0f, 1.0f, -6,
                0.5f, 0.3f, 1f, 0x0000);

        // camera position label
        Vector3f pos = new Vector3f(camera.getPosition());
        renderer.drawString(
                String.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z),
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
            hud.setShowInventory(!hud.isShowInventory());
            if (hud.isShowInventory()) {
                float x = Application.getWindow().getWidth() / 2.0f;
                float y = Application.getWindow().getHeight() / 2.0f;
                Application.getWindow().setCursorPos(x, y);
            }
        }
        if (isKeyBeginPress(GLFW_KEY_RIGHT)) {
            Inventory.Hotbar hotbar = hud.getInventory().getHotbar();
            hotbar.next();
        }
        if (isKeyBeginPress(GLFW_KEY_LEFT)) {
            Inventory.Hotbar hotbar = hud.getInventory().getHotbar();
            hotbar.previous();
        }
        if (isKeyBeginPress(GLFW_KEY_P)) {
            Application.takeScreenshot("screenshot");
        }

        hud.processInput(dt);
    }

    public World getWorld() {
        return world;
    }

    public Camera getCamera() {
        return camera;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public void addGameObject(GameObject go) {
        if (world != null) {
            world.addGameObject(go);
        }
    }

    public List<GameObject> getGameObjects() {
        return (world != null) ? world.getGameObjects() : null;
    }

    public GameObject getGameObject(int id) {
        return (world != null) ? world.getGameObject(id) : null;
    }

    public GameObject getGameObject(String name) {
        return (world != null) ? world.getGameObject(name) : null;
    }

    public <T extends Component> GameObject getGameObject(Class<T> componentClass) {
        return (world != null) ? world.getGameObject(componentClass) : null;
    }

    public <T extends Component> List<GameObject> getGameObjects(Class<T> componentClass) {
        return (world != null) ? world.getGameObjects(componentClass) : null;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (world != null) ? world.getComponent(componentClass) : null;
    }
}
