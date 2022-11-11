package com.github.wnebyte.minecraft.world;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.components.Inventory;
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

    private Skybox skybox;

    private Physics physics;

    private float time;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    private Vector3f lastCameraPos;

    private List<GameObject> gameObjects;

    private GameObject sun;

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
        this.skybox = new Skybox(camera);
        this.physics = new Physics(map);
        this.gameObjects = new ArrayList<>();
        GameObject cameraGo = new GameObject("Camera");
        cameraGo.addComponent(camera);
        gameObjects.add(cameraGo);
        GameObject playerGo = new GameObject("Player");
        playerGo.addComponent(new PlayerController());
        playerGo.addComponent(new Inventory());
        gameObjects.add(playerGo);
        sun = Prefabs.createSun(200, 200, 200, 20f);
        gameObjects.add(sun);

    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start(Scene scene) {
        for (GameObject go : gameObjects) {
            go.start(scene);
        }
        skybox.start();
        chunkManager.start();
        camera.setPosition(new Vector3f(CHUNK_SPAWN_AREA / 2.0f, 140, CHUNK_SPAWN_AREA / 2.0f));
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

        // set skybox blend value
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        skybox.setBlend(blend);
        chunkManager.setSunPosition(sun.transform.position);

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
        chunkManager.render();
    }

    public void destroy() {
        skybox.destroy();
        chunkManager.destroy();
        for (GameObject go : gameObjects) {
            go.destroy();
        }
    }

    public Map getMap() {
        return map;
    }

    public Physics getPhysics() {
        return physics;
    }

    public void addGameObject(GameObject go) {
        gameObjects.add(go);
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public GameObject getGameObject(int id) {
        return gameObjects.stream().filter(go -> go.getId() == id)
                .findFirst().orElse(null);
    }

    public GameObject getGameObject(String name) {
        return gameObjects.stream().filter(go -> go.getName().equals(name))
                .findFirst().orElse(null);
    }

    public <T extends Component> GameObject getGameObject(Class<T> componentClass) {
        return gameObjects.stream().filter(go -> go.getComponent(componentClass) != null)
                .findFirst().orElse(null);
    }

    public <T extends Component> List<GameObject> getGameObjects(Class<T> componentClass) {
        return gameObjects.stream().filter(go -> go.getComponent(componentClass) != null)
                .collect(Collectors.toList());
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        GameObject go = getGameObject(componentClass);
        return (go != null) ? go.getComponent(componentClass) : null;
    }
}
