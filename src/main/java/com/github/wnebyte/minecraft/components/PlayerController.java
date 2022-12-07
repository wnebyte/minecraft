package com.github.wnebyte.minecraft.components;

import java.util.Random;

import com.github.wnebyte.minecraft.world.*;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;

import static com.github.wnebyte.minecraft.core.MouseListener.isMouseButtonDown;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class PlayerController extends Component {

    private transient Camera camera;

    private transient Inventory inventory;

    private transient Renderer renderer;

    private transient Physics physics;

    private transient Map map;

    private transient RaycastInfo info;

    private transient Random rand;

    private transient float destroyBlockDebounceTime = 0.2f;

    private transient float destroyBlockDebounce = destroyBlockDebounceTime;

    private transient float placeBlockDebounceTime = 0.2f;

    private transient float placeBlockDebounce = placeBlockDebounceTime;

    public PlayerController() {
        this.rand = new Random();
    }

    @Override
    public void start(Scene scene) {
        this.camera = scene.getCamera();
        this.renderer = scene.getRenderer();
        this.map = scene.getWorld().getMap();
        this.physics = scene.getWorld().getPhysics();
        this.inventory = gameObject.getComponent(Inventory.class);
    }

    @Override
    public void update(float dt) {
        destroyBlockDebounce -= dt;
        placeBlockDebounce -= dt;

        /*
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f forward = new Vector3f(camera.getForward());
        RaycastInfo raycast = physics.raycast(origin, forward, 15f);

        if (raycast.isHit()) {
            info = raycast;
            renderer.drawBox3D(info.getCenter(), info.getSize(), 0f,
                    new Vector3f(1f, 1f, 1f));
        }
         */

        // destroy block
        if (isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && info != null && info.isHit() &&
                destroyBlockDebounce <= 0) {

        }

        // place block
        if (isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && info != null && info.isHit() &&
                placeBlockDebounce <= 0) {

        }
    }

    private void placeBlock(Block b) {
        Chunk chunk = map.getChunk(info.getCenter());
        if (chunk != null && info.getCenter().y + 1 < Chunk.HEIGHT) {
            Vector3i ivec3 = Chunk.world2Index3D(info.getCenter().add(0, 1, 0), chunk.getChunkCoords());
            chunk.setBlock(b, ivec3.x, ivec3.y, ivec3.z, true);
        }
        info = null;
        placeBlockDebounce = placeBlockDebounceTime;
    }

    private Block destroyBlock() {
        Chunk chunk = map.getChunk(info.getCenter());
        if (chunk != null) {
            Vector3i ivec3 = Chunk.world2Index3D(info.getCenter(), chunk.getChunkCoords());
            Block b = chunk.getBlock(ivec3);
            chunk.setBlock(BlockMap.getBlock("air"), ivec3.x, ivec3.y, ivec3.z, true);
            return Block.isAir(b) ? null : b;
        }
        info = null;
        destroyBlockDebounce = destroyBlockDebounceTime;
        return null;
    }
}