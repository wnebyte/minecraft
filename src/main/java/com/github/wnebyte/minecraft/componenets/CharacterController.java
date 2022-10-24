package com.github.wnebyte.minecraft.componenets;

import com.github.wnebyte.minecraft.core.KeyListener;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Camera;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

public class CharacterController extends Component {

    public float baseSpeed;

    public float runSpeed;

    public float movementSensitivity;

    public float jumpForce;

    public float downJumpForce;

    public Vector3f cameraOffset;

    public Vector3f movementAxis;

    public Vector2f viewAxis;

    public boolean isRunning;

    public boolean lockedToCamera;

    public boolean applyJumpForce;

    public boolean inMiddleOfJump;

    public Camera camera;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    @Override
    public void update(float dt) {
        if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
            float velocity = baseSpeed * dt;
            camera.getPosition().add((new Vector3f(camera.getForward().mul(velocity))));
        }
    }
}
