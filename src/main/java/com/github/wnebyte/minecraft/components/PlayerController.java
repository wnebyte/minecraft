package com.github.wnebyte.minecraft.components;

import java.util.Random;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import com.github.wnebyte.minecraft.world.BlockMap;
import static com.github.wnebyte.minecraft.core.MouseListener.isMouseButtonDown;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class PlayerController extends Component {

    private transient Camera camera;

    private transient Renderer renderer;

    private transient Physics physics;

    private transient Map map;

    private transient RaycastInfo info;

    private transient Random rand;

    private transient float destroyBlockDebounceTime = 0.2f;

    private transient float destroyBlockDebounce = destroyBlockDebounceTime;

    private transient float placeBlockDebounceTime = 0.2f;

    private transient float placeBlockDebounce = placeBlockDebounceTime;

    public PlayerController(Physics physics, Map map) {
        this.physics = physics;
        this.map = map;
        this.renderer = Renderer.getInstance();
        this.rand = new Random();
    }

    @Override
    public void start(Scene scene) {
        camera = scene.getCamera();
    }

    @Override
    public void update(float dt) {
        destroyBlockDebounce -= dt;
        placeBlockDebounce -= dt;

        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f forward = new Vector3f(camera.getForward());
        RaycastInfo raycast = physics.raycast(origin, forward, 15f);

        if (raycast.isHit()) {
            info = raycast;
            renderer.drawBox3D(info.getBlockCenter(), info.getBlockSize(), 0f,
                    new Vector3f(1f, 1f, 1f));
        }

        // destroy block
        if (isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && info != null && info.isHit() &&
                destroyBlockDebounce <= 0) {
            Chunk chunk = map.getChunk(info.getBlockCenter());
            if (chunk != null) {
                Vector3i ivec3 = Chunk.world2Index3D(info.getBlockCenter(), chunk.getChunkCoords());
                Block b = BlockMap.getBlock("air");
                chunk.setBlock(b, ivec3.x, ivec3.y, ivec3.z, true);
            }
            info = null;
            destroyBlockDebounce = destroyBlockDebounceTime;
        }

        // place block
        if (isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && info != null && info.isHit() &&
                placeBlockDebounce <= 0) {
            Chunk chunk = map.getChunk(info.getBlockCenter());
            if (chunk != null && info.getBlockCenter().y + 1 < Chunk.HEIGHT) {
                Vector3i ivec3 = Chunk.world2Index3D(info.getBlockCenter().add(0, 1, 0), chunk.getChunkCoords());
                Block b = BlockMap.getBlock(rand.nextBoolean() ? 18 : 15);
                chunk.setBlock(b, ivec3.x, ivec3.y, ivec3.z, true);
            }
            info = null;
            placeBlockDebounce = placeBlockDebounceTime;
        }
    }
}