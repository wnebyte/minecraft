package com.github.wnebyte.minecraft.core;

import java.util.List;
import org.joml.*;
import com.github.wnebyte.minecraft.world.*;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.event.EventSystem;

public class Scene {

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    protected static final float CROSSHAIR_SIZE = 0.08f;

    protected static final float CROSSHAIR_HALF_SIZE = CROSSHAIR_SIZE / 2.0f;

    protected static final Vector3f CROSSHAIR_COLOR = new Vector3f(177.0f / 255.0f, 199.0f / 255.0f, 179.0f / 255.0f);

    protected static final Camera DEFAULT_CAMERA = new Camera.Builder()
            .setPosition(0.0f, 0.0f, 3.0f)
            .setMovementSpeed(10f)
            .setZFar(10_000f)
            .build();

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    protected Camera camera;

    protected Renderer renderer;

    protected EventSystem eventSystem;

    protected World world;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Scene(Camera camera) {
        this.camera = camera;
        this.renderer = Renderer.getInstance();
        this.eventSystem = new EventSystem();
        this.world = null;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start() {}

    public void update(float dt) {}

    public void render() {}

    public void destroy() {}

    public void processInput(float dt) {}

    protected void drawCursor(float x, float y, float halfSize, Vector3f color) {
        renderer.drawLine2D(
                new Vector2f(x, y - halfSize),
                new Vector2f(x, y + halfSize),
                0,
                color);
        renderer.drawLine2D(
                new Vector2f(x - halfSize, y),
                new Vector2f(x + halfSize, y),
                0,
                color);
    }

    public World getWorld() {
        return world;
    }

    public Camera getCamera() {
        return camera;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public void addGameObject(GameObject go) {
        if (world != null) {
            world.addGameObject(go);
        }
    }

    public void destroyGameObject(GameObject go) {
        if (world != null) {
            world.destroyGameObject(go);
        }
    }

    public List<GameObject> getGameObjects() {
        return (world != null) ? world.getGameObjects() : null;
    }

    public GameObject getGameObject(int id) {
        return (world != null) ? world.getGameObject(id) : null;
    }

    public GameObject getGameObject(String name) {
        return (world != null) ? world.getGameObject(name) : null;
    }

    public <T extends Component> GameObject getGameObject(Class<T> componentClass) {
        return (world != null) ? world.getGameObject(componentClass) : null;
    }

    public <T extends Component> List<GameObject> getGameObjects(Class<T> componentClass) {
        return (world != null) ? world.getGameObjects(componentClass) : null;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (world != null) ? world.getComponent(componentClass) : null;
    }
}
