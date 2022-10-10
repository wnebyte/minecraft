package com.github.wnebyte.minecraft.world;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.componenets.BoxRenderer;
import com.github.wnebyte.minecraft.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

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

    public static final int CHUNK_CAPACITY = 50;

    public static final int SPAWN_CHUNK_SIZE = 5 * 5;

    private static final Vector4f SUN_COLOR = new Vector4f(1f, 1f, 1f, 1f);
            // = new Vector4f(252f / 255f, 248f / 255f, 3f / 255f, 1.0f);

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
        camera.setPosition(new Vector3f(-1, 51, 0));
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
                chunk.setBlock(Block.DIRT, index.x, index.y + 1, index.z, true);
                block = null;
                renderer.clearLines3D();
            }
            placeBlockDebounce = placeBlockDebounceTime;
        }
    }

    public void render() {
        // render skybox
        skybox.render();
        // render chunks
        chunkManager.render();
        // render game objects
        renderer.render();
    }

    public void destroy() {
        skybox.destroy();
        chunkManager.destroy();
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

}
