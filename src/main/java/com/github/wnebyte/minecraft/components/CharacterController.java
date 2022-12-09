package com.github.wnebyte.minecraft.components;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.KeyListener;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import static org.lwjgl.glfw.GLFW.*;

public class CharacterController extends Component {

    public float baseMovementSpeed = 7.5f;

    public float runMovementSpeed = baseMovementSpeed * 1.5f;

    public boolean isRunning;

    public Camera camera;

    private Renderer renderer;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    @Override
    public void start(Scene scene) {
        camera = scene.getCamera();
        renderer = scene.getRenderer();
    }

    @Override
    public void update(float dt) {
        if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
            handleMovement(Camera.Movement.FORWARD, dt);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
            handleMovement(Camera.Movement.BACKWARD, dt);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
            handleMovement(Camera.Movement.LEFT, dt);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
            handleMovement(Camera.Movement.RIGHT, dt);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            handleMovement(Camera.Movement.UP, dt);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            handleMovement(Camera.Movement.DOWN, dt);
        }
    }

    private void handleMovement(Camera.Movement direction, float dt) {
        float velocity = baseMovementSpeed * dt;
        Vector3f position = gameObject.transform.position;
        Vector3f forward = new Vector3f(camera.getForward());
        forward.y = 0.0f;
        switch (direction) {
            case FORWARD:
                position.add(new Vector3f(forward).mul(velocity));
                break;
            case BACKWARD:
                position.sub(new Vector3f(forward).mul(velocity));
                break;
            case LEFT:
                position.sub(new Vector3f(camera.getRight()).mul(velocity));
                break;
            case RIGHT:
                position.add(new Vector3f(camera.getRight()).mul(velocity));
                break;
            case UP:
                position.add(new Vector3f(camera.getUp()).mul(velocity));
                break;
            case DOWN:
                position.sub(new Vector3f(camera.getUp()).mul(velocity));
                break;
        }
    }
}
