package com.github.wnebyte.minecraft.components;

import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.world.*;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.util.JColor;
import static com.github.wnebyte.minecraft.core.MouseListener.isMouseButtonBeginDown;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;

public class PlayerController extends Component {

    private float baseMovementSpeed = 7.5f;

    private float runMovementSpeed = baseMovementSpeed * 1.5f;

    private boolean isRunning = false;

    private boolean isJumping = false;

    private float jumpBoost = 1.0f;

    private float jumpImpulse = 7.5f;

    private boolean onGround = false;

    private Rigidbody rb;

    private transient Camera camera;

    private transient Inventory inventory;

    private transient Renderer renderer;

    private transient Physics physics;

    private transient Map map;

    private transient RaycastInfo info;

    private transient float destroyBlockDebounceTime = 0.2f;

    private transient float destroyBlockDebounce = destroyBlockDebounceTime;

    private transient float placeBlockDebounceTime = 0.2f;

    private transient float placeBlockDebounce = placeBlockDebounceTime;

    public PlayerController() {}

    @Override
    public void start(Scene scene) {
        camera = scene.getCamera();
        renderer = scene.getRenderer();
        map = scene.getWorld().getMap();
        physics = scene.getWorld().getPhysics();
        inventory = gameObject.getComponent(Inventory.class);
        rb = gameObject.getComponent(Rigidbody.class);
    }

    @Override
    public void update(float dt) {
        destroyBlockDebounce -= dt;
        placeBlockDebounce -= dt;
        onGround = true;

        // jump
        if (isKeyBeginPress(GLFW_KEY_SPACE)) {
            if (onGround) {
                rb.velocity.y = (jumpBoost * jumpImpulse);
            }
        }
        // forward movement
        if (isKeyPressed(GLFW_KEY_W)) {
            handleMovement(Camera.Movement.FORWARD, dt);
        }
        // backward movement
        if (isKeyPressed(GLFW_KEY_S)) {
            handleMovement(Camera.Movement.BACKWARD, dt);
        }
        // left movement
        if (isKeyPressed(GLFW_KEY_A)) {
            handleMovement(Camera.Movement.LEFT, dt);
        }
        // right movement
        if (isKeyPressed(GLFW_KEY_D)) {
            handleMovement(Camera.Movement.RIGHT, dt);
        }

        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f forward = new Vector3f(camera.getForward());
        RaycastInfo raycast = physics.raycast(origin, forward, 15f);

        if (raycast.isHit()) {
            info = raycast;
            renderer.drawBox3D(info.getCenter(), info.getSize(), 0f, JColor.WHITE_VEC3, 4f);
        }

        // destroy block
        if (isMouseButtonBeginDown(GLFW_MOUSE_BUTTON_LEFT) && info != null && info.isHit() &&
                destroyBlockDebounce <= 0) {
            destroyBlock();
        }

        // place block
        if (isMouseButtonBeginDown(GLFW_MOUSE_BUTTON_RIGHT) && info != null && info.isHit() &&
                placeBlockDebounce <= 0) {
            Block b = BlockMap.getBlock("sand");
            placeBlock(b);
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

    private void placeBlock(Block b) {
        Chunk chunk = map.getChunk(info.getCenter());
        if (chunk != null && info.getCenter().y + 1 < Chunk.HEIGHT) {
            Vector3i index3D = Chunk.world2Index3D(info.getCenter().add(0, 1, 0), chunk.getChunkCoords());
            chunk.setBlock(b, index3D.x, index3D.y, index3D.z, true);
        }
        info = null;
        placeBlockDebounce = placeBlockDebounceTime;
    }

    private Block destroyBlock() {
        Chunk chunk = map.getChunk(info.getCenter());
        if (chunk != null) {
            Vector3i index3D = Chunk.world2Index3D(info.getCenter(), chunk.getChunkCoords());
            Block b = chunk.getBlock(index3D);
            chunk.setBlock(BlockMap.getBlock("air"), index3D.x, index3D.y, index3D.z, true);
            return Block.isAir(b) ? null : b;
        }
        info = null;
        destroyBlockDebounce = destroyBlockDebounceTime;
        return null;
    }
}