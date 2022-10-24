package com.github.wnebyte.minecraft.world;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.componenets.BoxRenderer;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.util.*;
import static org.lwjgl.glfw.GLFW.*;

public class World {

    public static GameObject createGameObject(String name, float x, float y, float z, float scale) {
        GameObject go = new GameObject(name);
        Transform transform = new Transform(new Vector3f(x, y, z), new Vector3f(scale, scale, scale), 0f);
        go.addComponent(transform);
        go.transform = transform;
        return go;
    }

    public static GameObject createSun(float x, float y, float z, float scale, Vector4f color) {
        GameObject go = createGameObject("Sun", x, y, z, scale);
        BoxRenderer box = new BoxRenderer();
        box.setColor(color);
        go.addComponent(box);
        return go;
    }

    public static GameObject createPlayer(float x, float y, float z, float scale) {
        GameObject go = createGameObject("Player", x, y, z, scale);
        go.addComponent(new Transform(new Vector3f(x, y, z), new Vector3f(scale, scale, scale)));
        go.transform = go.getComponent(Transform.class);
        Rigidbody rb = new Rigidbody();
        go.addComponent(rb);
        BoxCollider bc = new BoxCollider();
        bc.setSize(new Vector3f(1f, 1f, 1f));
        bc.setOffset(new Vector3f(0f, 0f, 0f));
        go.addComponent(bc);
        return go;
    }

    public static final int CHUNK_CAPACITY = 500;

    public static final int CHUNK_RADIUS = 5;

    public static final int SPAWN_CHUNK_SIZE = 9 * 9;

    private static final Vector4f SUN_COLOR = new Vector4f(1f, 1f, 1f, 1f);

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

    private Random rand;

    private Vector3f block;

    public World(Camera camera, Renderer renderer) {
        this.camera = camera;
        this.map = new Map();
        this.chunkManager = new ChunkManager(camera, map);
        this.renderer = renderer;
        this.skybox = new Skybox(camera);
        this.physics = new Physics(renderer, chunkManager.getMap());
        this.sun = createSun(400, 80f, 50f, 10f, SUN_COLOR);
        this.gameObjects = new ArrayList<>();
        this.gameObjects.add(sun);
        this.rand = new Random();
    }

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
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        if (time == 1.0f) time = 0.0f;
        skybox.setBlend(blend);

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

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && block != null && debounce < 0) {
            Chunk chunk = map.getChunk(block.x, block.y, block.z);
            if (chunk != null) {
                Vector3i index = Chunk.world2Index3D(block, chunk.getChunkCoords());
                chunk.setBlock(Block.AIR, index.x, index.y, index.z, true);
                block = null;
                renderer.clearLines3D();
            }
            debounce = debounceTime;
        }

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && block != null && placeBlockDebounce < 0) {
            Chunk chunk = map.getChunk(block.x, block.y, block.z);
            if (chunk != null && (block.y + 1) < Chunk.HEIGHT) {
                Vector3i index = Chunk.world2Index3D(block, chunk.getChunkCoords());
                chunk.setBlock(Block.SAND, index.x, index.y + 1, index.z, true);
                block = null;
                renderer.clearLines3D();
            }
            placeBlockDebounce = placeBlockDebounceTime;
        }

        if (debounce < 0) {
            Vector2i v = Chunk.toChunkCoords2D(new Vector3f(camera.getPosition()));

            Set<Chunk> chunks = map.getChunksBeyondRadius(v, CHUNK_RADIUS);
            for (Chunk chunk : chunks) {
                chunkManager.unloadChunk(chunk);
            }

            Set<Vector2i> chunkCoords = map.getChunkCoordsWithinRadius(v, CHUNK_RADIUS);
            for (Vector2i ivec2 : chunkCoords) {
                assert (chunks.stream().noneMatch(c -> c.getChunkCoords().equals(ivec2))) :
                        "Loading recently unloaded chunk";
                chunkManager.loadChunk(ivec2);
            }

            /*
            System.out.printf("Number of loaded chunks, subchunks: %d:%d%n",
                    map.size(),
                    ((chunkManager.getSubchunks().size() / 2) / 16));
             */

            debounce = debounceTime;
        }

    }

    private String toString(Set<Vector2i> set) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (Vector2i ivec2 : set) {
            b.append(String.format("(%d, %d)", ivec2.x, ivec2.y));
        }
        b.append("]");
        return b.toString();
    }

    public void render(float dt) {
        // render skybox
        skybox.render();
        // render chunks
        chunkManager.render(dt);
    }

    public void destroy() {
        skybox.destroy();
        chunkManager.destroy();
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

}
