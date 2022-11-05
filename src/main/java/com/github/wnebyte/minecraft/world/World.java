package com.github.wnebyte.minecraft.world;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.components.PlayerController;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;

public class World {

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int CHUNK_RADIUS = 12;

    public static final int CHUNK_CAPACITY = 2 * (CHUNK_RADIUS * CHUNK_RADIUS);

    public static final int CHUNK_SPAWN_AREA = 9 * 9;

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

    private Vector3f lastCameraPos;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public World(Camera camera) {
        this.camera = camera;
        this.lastCameraPos = new Vector3f(camera.getPosition());
        this.map = new Map(CHUNK_CAPACITY);
        this.chunkManager = new ChunkManager(camera, map);
        this.renderer = Renderer.getInstance();
        this.skybox = new Skybox(camera);
        this.physics = new Physics(renderer, map);
        this.gameObjects = new ArrayList<>();
        GameObject go = new GameObject("Camera");
        go.addComponent(camera);
        go.addComponent(new Transform());
        this.gameObjects.add(go);
        GameObject player = new GameObject("Player");
        player.addComponent(new Transform());
        player.addComponent(new PlayerController(physics, map));
       // this.gameObjects.add(player);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start(Scene scene) {
        chunkManager.start();
        skybox.start();
        for (GameObject go : gameObjects) {
            go.start(scene);
        }
        camera.setPosition(new Vector3f(CHUNK_SPAWN_AREA / 2.0f, 140, CHUNK_SPAWN_AREA / 2.0f));
       // chunkManager.loadSpawnChunks();
    }

    /*
    0.016666667 ticks / second
    1           tick  / minute
    60          ticks / hour
    1440        ticks / day
     */
    public void update(float dt) {
        debounce -= dt;

        // set skybox blend value
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        skybox.setBlend(blend);

        // update game objects
        for (GameObject go : gameObjects) {
            go.update(dt);
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
        skybox.render();
       // chunkManager.render();
    }

    public void destroy() {
        skybox.destroy();
        chunkManager.destroy();
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

}
