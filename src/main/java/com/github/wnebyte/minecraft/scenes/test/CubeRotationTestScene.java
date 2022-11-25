package com.github.wnebyte.minecraft.scenes.test;

import org.joml.Vector3f;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.Cube3D;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.world.BlockMap;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;

public class CubeRotationTestScene {

    private Camera camera;

    private Renderer renderer;

    private int index = 0;

    private Vector4f[] rotations = {
            new Vector4f(7.0f, 1.0f, 0.0f, 0.0f),
            new Vector4f(19.5f, 0.0f, 1.0f, 0.0f),
            new Vector4f(0.5f, 0.0f, 0.0f, 1.0f)
    };

    private float yaw = -63.70f;

    private float pitch = -44.34f;

    private float fov = 41.0f;

    private Vector3f position = new Vector3f(-2.25f, 2.36f, 3.52f);

    private Camera c = new Camera(
            new Vector3f(position),                   // position
            new Vector3f(0.0f, 0.0f, -1.0f), // forward
            new Vector3f(0.0f, 1.0f, 0.0f),  // up
            yaw,
            pitch,
            10f,
            Camera.DEFAULT_MOUSE_SENSITIVITY,
            fov,
            Camera.DEFAULT_Z_NEAR,
            10_000f);

    private float scale = 2.25f;

    private boolean drawLbls = false;

    public CubeRotationTestScene() {

    }


    public void start() {
       // super.loadResources();
    }


    public void update(float dt) {
        camera.update(dt);
        Vector4f rotation = rotations[index];
        drawCube();
        if (drawLbls) {
            drawRotationLabels(rotation);
            drawCameraLabels(camera);
        }
    }

    private void drawCube() {
        renderer.drawCube3D(new Cube3D(new Transform(
                new Vector3f(0f, 0f, -1),
                new Vector3f(scale, scale, scale),
                rotations),
                new Vector3f(1f, 1f, 1f),
                BlockMap.getTextureFormat("grass_block_side").getAsSprite(),
                BlockMap.getTextureFormat("grass_block_top").getAsSprite(),
                BlockMap.getTextureFormat("dirt").getAsSprite()));
        renderer.flushCube3DBatches(camera.getViewMatrix(), camera.getProjectionMatrix());
    }

    private void drawRotationLabels(Vector4f rotation) {
        renderer.drawString(
                String.format("I: %d", index),
                -3.0f + 0.05f, 1.2f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("ANG: %.2f", rotation.x),
                -3.0f + 0.05f, 1.1f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("X: %.0f", rotation.y),
                -3.0f + 0.05f, 1.0f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("Y: %.0f", rotation.z),
                -3.0f + 0.05f, 0.9f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("Z: %.0f", rotation.w),
                -3.0f + 0.05f, 0.8f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("SCALE: %.2f", scale),
                -3.0f + 0.05f, 0.7f, 0, 0.0040f, 0xFFFF);
    }

    private void drawCameraLabels(Camera camera) {
        Vector3f position = camera.getPosition();
        renderer.drawString(
                String.format("POS: %.2f, %.2f, %.2f", position.x, position.y, position.z),
                -3.0f + 0.05f, 0.6f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("YAW: %.2f", camera.getYaw()),
                -3.0f + 0.05f, 0.5f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("PITCH: %.2f", camera.getPitch()),
                -3.0f + 0.05f, 0.4f, 0, 0.0040f, 0xFFFF);
        renderer.drawString(
                String.format("FOV: %.2f", camera.getZoom()),
                -3.0f + 0.05f, 0.3f, 0, 0.0040f, 0xFFFF);
    }


    public void render() {
        renderer.flush(camera);
    }


    public Camera getCamera() {
        return camera;
    }


    public void processInput(float dt) {
        Vector4f rotation = rotations[index];
        if (isKeyBeginPress(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setWindowShouldClose(true);
        }
        if (isKeyPressed(GLFW_KEY_RIGHT)) {
            rotation.x += 0.5f;
        }
        if (isKeyPressed(GLFW_KEY_LEFT)) {
            rotation.x -= 0.5f;
        }
        if (isKeyBeginPress(GLFW_KEY_X)) {
            rotation.y = (rotation.y == 0.0f) ? 1.0f : 0.0f;
        }
        if (isKeyBeginPress(GLFW_KEY_Y)) {
            rotation.z = (rotation.z == 0.0f) ? 1.0f : 0.0f;
        }
        if (isKeyBeginPress(GLFW_KEY_Z)) {
            rotation.w = (rotation.w == 0.0f) ? 1.0f : 0.0f;
        }
        if (isKeyBeginPress(GLFW_KEY_UP)) {
            index = (index + 1) % rotations.length;
        }
        if (isKeyBeginPress(GLFW_KEY_DOWN)) {
            index = (index + (rotations.length - 1)) % rotations.length;
        }
        if (isKeyBeginPress(GLFW_KEY_1)) {
            scale -= 0.25f;
        }
        if (isKeyBeginPress(GLFW_KEY_2)) {
            scale += 0.25f;
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
            camera.setZoom(fov);
            camera.setPitch(pitch);
            camera.setYaw(yaw);
            camera.setPosition(new Vector3f(position));
            camera.updateCameraVectors();
        }
        if (isKeyBeginPress(GLFW_KEY_T)) {
            drawLbls = !drawLbls;
        }
        if (isKeyBeginPress(GLFW_KEY_P)) {
            Application.takeScreenshot("64x64-non-pixelated");
        }
    }
}
