package com.github.wnebyte.minecraft.world;

import com.github.wnebyte.minecraft.componenets.BoxRenderer;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

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

    private static final Vector4f SUN_COLOR = new Vector4f(252f / 255f, 248f / 255f, 3f / 255f, 1.0f);

    private Camera camera;

    private ChunkManager chunkManager;

    private Renderer renderer;

    private Skybox skybox;

    private GameObject sun;

    private List<GameObject> gameObjects;

    private float time;

    private float debounceTime = 3.5f;

    private float debounce = debounceTime;

    public World(Camera camera) {
        this.camera = camera;
        this.chunkManager = new ChunkManager(camera);
        this.renderer = new Renderer(camera);
        this.skybox = new Skybox(camera);
        this.sun = createSun(400, 80f, 50f, 10f, SUN_COLOR);
        this.gameObjects = new ArrayList<>();
        this.gameObjects.add(sun);
    }

    public void start() {
        // start chunk manager
        chunkManager.start();
        // start skybox
        skybox.start();
        // start game objects
        for (GameObject go : gameObjects) {
            go.start();
            renderer.add(go);
        }
    }

    /*
    0.016666667 ticks / second
    1           tick  / minute
    60          ticks / hour
    1440        ticks / day
     */
    public void update(float dt) {
        debounce -= dt;
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        if (time == 1.0f) time = 0.0f;
        skybox.setBlend(blend);

        if (debounce < 0) {
            JMath.subX(sun.transform.position, dt * 2);
            debounce = debounceTime;
        }

        for (GameObject go : gameObjects) {
            go.update(dt);
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
