package com.github.wnebyte.minecraft.scenes;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.ui.InfoWindow;
import com.github.wnebyte.minecraft.ui.JGuiWindow;
import com.github.wnebyte.minecraft.world.World;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.ui.Hud;
import org.joml.Vector3f;

import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

public class GameScene extends Scene {

    private boolean started;

    private final Hud hud;

    private final List<JGuiWindow> windows;

    private final AtomicLong counter;

    public GameScene(Camera camera) {
        super(camera);
        this.world = new World(camera);
        this.hud = new Hud(camera);
        this.windows = new ArrayList<>();
        this.windows.add(new InfoWindow(camera));
        this.counter = new AtomicLong(0L);
    }

    @Override
    public void start() {
        if (!started) {
            world.start(this);
            hud.start(this);
            world.load(counter);
            started = true;
        }
    }

    @Override
    public void update(float dt) {
        for (JGuiWindow window : windows) {
            window.update(dt);
        }
        world.update(dt);
        hud.update(dt);
    }

    @Override
    public void processInput(float dt) {
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setShouldClose(true);
        }
        if (isKeyPressed(GLFW_KEY_KP_ADD)) {
            Vector3f offset = camera.getOffset();
            offset.z += 0.5f;
        }
        if (isKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
            Vector3f offset = camera.getOffset();
            offset.z -= 0.5f;
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

    @Override
    public void render() {
        world.render();
        renderer.flush(camera);
    }

    @Override
    public void destroy() {
        world.destroy();
        renderer.destroy();
    }

    public long getCounter() {
        return counter.get();
    }
}
