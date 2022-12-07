package com.github.wnebyte.minecraft.world;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
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

    private final Camera camera;

    private final Map map;

    private final Pool<Vector3i, Subchunk> subchunks;

    private final ChunkRenderer chunkRenderer;

    private final Skybox skybox;

    private final Physics physics;

    private final List<GameObject> gameObjects;

    private final Vector3f lastCameraPos;

    private float time;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public World(Camera camera) {
        this.camera = camera;
        this.lastCameraPos = new Vector3f(camera.getPosition());
        this.map = new Map(CHUNK_CAPACITY);
        this.subchunks = new Pool<>(World.CHUNK_CAPACITY * 16);
        this.chunkRenderer = new ChunkRenderer(camera, subchunks);
        this.skybox = new Skybox(camera);
        this.physics = new Physics(map);
        this.gameObjects = new ArrayList<>();
        GameObject playerGo = Prefabs.createPlayer(0, 0, 0, 1f);
        playerGo.addComponent(camera);
        gameObjects.add(playerGo);
        GameObject sunGo = Prefabs.createSun(200, 200, 200, 20f);
        gameObjects.add(sunGo);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start(Scene scene) {
        skybox.start();
        chunkRenderer.start();
        for (GameObject go : gameObjects) {
            go.start(scene);
            physics.add(go);
        }
        Vector3f pos = new Vector3f(CHUNK_SPAWN_AREA / 2.0f, 140f, CHUNK_SPAWN_AREA / 2.0f);
        GameObject playerGo = getGameObject("Player");
        playerGo.transform.position.set(pos);
        camera.setOffset(new Vector3f(0, 2f, -5f));
    }

    public void update(float dt) {
        debounce -= dt;

        // set skybox blend value
        time += (dt / 6);
        float blend = JMath.clamp((time / 1440), 0.0f, 1.0f);
        skybox.setBlend(blend);

        for (GameObject go : gameObjects) {
            go.update(dt);
        }

        // load/unload chunks
        if (!camera.getPosition().equals(lastCameraPos) && debounce <= 0) {
            Vector2i v = Chunk.toChunkCoords(camera.getPosition());

            Set<Chunk> chunks = map.getChunksBeyondRadius(v, CHUNK_RADIUS);
            if (chunks.size() > 0) {
                this.unloadChunksAsync(chunks);
            }

            Set<Vector2i> chunkCoords = map.getChunkCoordsWithinRadius(v, CHUNK_RADIUS);
            for (Vector2i ivec2 : chunkCoords) {
                assert (chunks.stream().noneMatch(c -> c.getChunkCoords().equals(ivec2))) :
                        "Loading recently unloaded chunk";
            }
            if (chunkCoords.size() > 0) {
                this.loadChunksAsync(chunkCoords, null);
            }

            lastCameraPos.set(camera.getPosition());
            debounce = debounceTime;
        }

        physics.update(dt);
    }

    public void load(AtomicLong counter) {
        Set<Vector2i> set = new HashSet<>(World.CHUNK_SPAWN_AREA);
        int sqrt = (int)Math.sqrt(World.CHUNK_SPAWN_AREA);
        for (int x = 0; x < sqrt; x++) {
            for (int z = 0; z < sqrt; z++) {
                set.add(new Vector2i(x, z));
            }
        }
        this.loadChunksAsync(set, counter);
    }

    /*
    Step 1: Deserialize all chunks
    Step 2: Retrieve all neighbours
    Step 3: Mesh all newly loaded chunks and their neighbours
     */
    private void loadChunksAsync(Set<Vector2i> set, AtomicLong counter) {
        Application.submit(() -> {
            Set<Chunk> chunks = new HashSet<>(set.size() * 4);
            List<Future<Chunk>> futures = new ArrayList<>(set.size());
            for (Vector2i ivec2 : set) {
                if (!map.contains(ivec2) && map.size() < World.CHUNK_CAPACITY) {
                    Chunk chunk = new Chunk(ivec2.x, 0, ivec2.y, map, subchunks);
                    map.put(chunk);
                    chunks.add(chunk);
                    futures.add(chunk.loadAsync());
                    for (Chunk c : chunk.getNeighbours()) {
                        if (c != null) {
                            chunks.add(c);
                        }
                    }
                }
            }
            try {
                for (Future<Chunk> it : futures) {
                    Chunk chunk = it.get();
                }
                for (Chunk c : chunks) {
                    assert c.isLoaded() : "Chunk is not loaded";
                    c.mesh();
                    if (counter != null) {
                        counter.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*
    Step 1: Serialize and unmesh all chunks that need to be unloaded
    Step 2: When Step 1 has completed; mesh all neighbours
     */
    // Todo: it's not necessary to wait for step 1 to complete in order to start step 2.
    private void unloadChunksAsync(Set<Chunk> chunks) {
        Application.submit(() -> {
            Set<Chunk> neighbours = new HashSet<>();
            List<Future<?>> futures = new ArrayList<>();
            for (Chunk chunk : chunks) {
                map.remove(chunk);
                futures.add(chunk.unloadAsync());
                for (Chunk c : chunk.getNeighbours()) {
                    if (c != null) {
                        neighbours.add(c);
                    }
                }
            }
            try {
                for (Future<?> it : futures) it.get();
                for (Chunk c : neighbours) {
                    if (c.isLoaded()) {
                        c.meshAsync();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void render() {
        skybox.render();
        chunkRenderer.render();
    }

    public void destroy() {
        skybox.destroy();
        chunkRenderer.destroy();
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
