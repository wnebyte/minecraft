package com.github.wnebyte.minecraft.world;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import static org.lwjgl.glfw.GLFW.*;

public class World {

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int CHUNK_CAPACITY = 500;

    public static final int CHUNK_RADIUS = 5;

    public static final int SPAWN_CHUNK_SIZE = 9 * 9;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private Camera camera;

    private Map map;

    private ChunkManager chunkManager;

    private Renderer renderer;

    private Skybox skybox;

    private Physics physics;

    private GameObject sun;

    private List<GameObject> gameObjects;

    private float time;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    private float raycastDebounceTime = 0.0f;

    private float raycastDebounce = raycastDebounceTime;

    private float placeBlockDebounceTime = 0.2f;

    private float placeBlockDebounce = placeBlockDebounceTime;

    private Vector3f block;

    private Vector3f lastCameraPos;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public World(Camera camera, Renderer renderer) {
        this.camera = camera;
        this.lastCameraPos = new Vector3f(camera.getPosition());
        this.map = new Map();
        this.chunkManager = new ChunkManager(camera, map);
        this.renderer = renderer;
        this.skybox = new Skybox(camera);
        this.physics = new Physics(renderer, map);
        this.sun = Prefabs.createSun(400, 80f, 50f, 10f, new Vector4f(1f, 1f, 1f, 1f));
        this.gameObjects = new ArrayList<>();
        this.gameObjects.add(sun);
        GameObject go = new GameObject("Camera");
        go.addComponent(camera);
        go.addComponent(new Transform());
        this.gameObjects.set(0, go);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start(Scene scene) {
        // start chunk manager
        chunkManager.start();
        // start skybox
        skybox.start();
        // start game objects
        for (GameObject go : gameObjects) {
            go.start();
            renderer.add(go);
        }
        camera.setPosition(new Vector3f(SPAWN_CHUNK_SIZE / 2.0f, 51, SPAWN_CHUNK_SIZE / 2.0f));
        chunkManager.loadSpawnChunks();
    }

    /*
    0.016666667 ticks / second
    1           tick  / minute
    60          ticks / hour
    1440        ticks / day
     */
    public void update(float dt) {
        debounce -= dt;
        raycastDebounce -= dt;
        placeBlockDebounce -= dt;

        // set skybox blend value
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        if (time == 1.0f) time = 0.0f;
        skybox.setBlend(blend);

        // update game objects
        for (GameObject go : gameObjects) {
            go.update(dt);
        }

        // do raycast
        if (raycastDebounce < 0) {
            Vector3f origin = new Vector3f(camera.getPosition());
            Vector3f normal = new Vector3f(camera.getForward());
            RaycastInfo info = physics.raycast(origin, normal, 15f);
            if (info.hit) {
                renderer.clearLines3D();
                renderer.addBox3D(info.blockCenter, info.blockSize, 0f,
                        new Vector3f(1f, 1f, 1f), 60 * 5);
                block = info.blockCenter;
            }
            raycastDebounce = raycastDebounceTime;
        }

        // destroy block
        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && block != null && debounce <= 0) {
            Chunk chunk = map.getChunk(block.x, block.y, block.z);
            if (chunk != null) {
                Vector3i index = Chunk.world2Index3D(block, chunk.getChunkCoords());
                chunk.setBlock(BlockMap.getBlock("air"), index.x, index.y, index.z, true);
                block = null;
                renderer.clearLines3D();
            }
            debounce = debounceTime;
        }

        // place block
        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && block != null && placeBlockDebounce <= 0) {
            Chunk chunk = map.getChunk(block.x, block.y, block.z);
            if (chunk != null && (block.y + 1) < Chunk.HEIGHT) {
                Vector3i index = Chunk.world2Index3D(block, chunk.getChunkCoords());
                chunk.setBlock(BlockMap.getBlock("sand"), index.x, index.y + 1, index.z, true);
                block = null;
                renderer.clearLines3D();
            }
            placeBlockDebounce = placeBlockDebounceTime;
        }

        // load/unload chunks
        if (!camera.getPosition().equals(lastCameraPos) && debounce <= 0) {
            Vector2i v = Chunk.toChunkCoords(camera.getPosition());

            Set<Chunk> chunks = map.getChunksBeyondRadius(v, CHUNK_RADIUS);
            if (chunks.size() > 0) {
                chunkManager.unloadChunksAsync(chunks);
            }

            Set<Vector2i> chunkCoords = map.getChunkCoordsWithinRadius(v, CHUNK_RADIUS);
            for (Vector2i ivec2 : chunkCoords) {
                assert (chunks.stream().noneMatch(c -> c.getChunkCoords().equals(ivec2))) :
                        "Loading recently unloaded chunk";
            }
            if (chunkCoords.size() > 0) {
                chunkManager.loadChunksAsync(chunkCoords);
            }

            lastCameraPos.set(camera.getPosition());
            debounce = debounceTime;
        }

    }

    public void render() {
        // render skybox
        skybox.render();
        // render chunks
        chunkManager.render();
    }

    public void destroy() {
        skybox.destroy();
        chunkManager.destroy();
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

}
