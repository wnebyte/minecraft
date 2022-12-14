package com.github.wnebyte.minecraft.scenes.test;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.*;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.ui.JGui;
import com.github.wnebyte.minecraft.util.JColor;
import com.github.wnebyte.minecraft.renderer.Cube3D;
import com.github.wnebyte.minecraft.scenes.LoadingScene;
import com.github.wnebyte.minecraft.world.BlockMap;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;

public class CollisionTestScene extends Scene {

    private Vector3f cameraPos;

    private float yaw, pitch, zoom;

    private Cube3D staticCube;

    private Cube3D dynamicCube;

    private Vector3f position = new Vector3f(-2f, 0f, -1f);

    private GameObject dynamicGo, staticGo;

    private Transform t1, t2;

    private BoxCollider bc1, bc2;

    public CollisionTestScene() {
        super(Scene.DEFAULT_CAMERA);
    }

    private GameObject createGameObject(Cube3D cube, Vector3f offset) {
        GameObject go = new GameObject("Go");
        go.transform = cube.getTransform();
        BoxCollider bc = new BoxCollider();
        bc.setOffset(new Vector3f(offset));
        bc.setSize(new Vector3f(cube.transform.scale));
        go.addComponent(bc);
        Rigidbody rb = new Rigidbody();
        rb.setAcceleration(new Vector3f());
        rb.setVelocity(new Vector3f());
        go.addComponent(rb);
        return go;
    }

    private void initEntities() {
        dynamicGo = createGameObject(dynamicCube, new Vector3f(0f, 0.5f, 0f));
        t1 = dynamicGo.transform;
        bc1 = dynamicGo.getComponent(BoxCollider.class);
        staticGo = createGameObject(staticCube, new Vector3f());
        t2 = staticGo.transform;
        bc2 = staticGo.getComponent(BoxCollider.class);
    }

    @Override
    public void start() {
       // LoadingScene.ResourceLoader loader = new LoadingScene.ResourceLoader();
       // loader.run();
        staticCube = new Cube3D(new Transform(
                new Vector3f(0f, 0f, -1),
                new Vector3f(1f, 1f, 1f)),
                new Vector3f(1f, 1f, 1f),
                BlockMap.getTextureFormat("grass_block_side").getAsSprite(),
                BlockMap.getTextureFormat("grass_block_top").getAsSprite(),
                BlockMap.getTextureFormat("dirt").getAsSprite());
        dynamicCube = new Cube3D(new Transform(
                position,
                new Vector3f(2f, 1f, 1f)),
                new Vector3f(1f, 1f, 1f),
                BlockMap.getTextureFormat("sand").getAsSprite(),
                BlockMap.getTextureFormat("sand").getAsSprite(),
                BlockMap.getTextureFormat("sand").getAsSprite());
        cameraPos = new Vector3f(camera.getPosition());
        yaw = camera.getYaw();
        pitch = camera.getPitch();
        zoom = camera.getZoom();
        initEntities();
    }

    @Override
    public void update(float dt) {
        camera.update(dt);
        Vector3f pos = camera.getPosition();
        /*
        float x = World.getPenetrationAmount(t1, bc1, t2, bc2, World.X_AXIS);
        float y = World.getPenetrationAmount(t1, bc1, t2, bc2, World.Y_AXIS);
        float z = World.getPenetrationAmount(t1, bc1, t2, bc2, World.Z_AXIS);
         */
        JGui.begin(-3.0f, 1.3f, 6.0f, 3.0f);
        JGui.label(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z), 0.0045f, JColor.WHITE_HEX);
        /*
        JGui.label(String.format("%.2f", x),                               0.0045f, JColor.WHITE_HEX);
        JGui.label(String.format("%.2f", y),                               0.0045f, JColor.WHITE_HEX);
        JGui.label(String.format("%.2f", z), 0.0045f, JColor.WHITE_HEX);
        if (World.isColliding(t1, bc1, t2, bc2)) {
            x = World.getNormalizedPenetrationAmount(t1, bc1, t2, bc2, World.X_AXIS);
            y = World.getNormalizedPenetrationAmount(t1, bc1, t2, bc2, World.Y_AXIS);
            z = World.getNormalizedPenetrationAmount(t1, bc1, t2, bc2, World.Z_AXIS);
            float absX = Math.abs(x);
            float absY = Math.abs(y);
            float absZ = Math.abs(z);

            if (absX < Math.min(absY, absZ)) {
                dynamicCube.transform.position.x += x;
            } else if (absY < Math.min(absX, absZ)) {
                dynamicCube.transform.position.y += y;
            } else {
                dynamicCube.transform.position.z += z;
            }
        }
         */
        JGui.end();
    }

    @Override
    public void render() {
        /*
        renderer.drawCube3D(dynamicCube);
        renderer.drawCube3D(staticCube);
        renderer.drawBox3D(new Vector3f(t1.position).add(bc1.getOffset()), bc1.getSize(), 0.0f, new Vector3f(252f / 255f, 248f / 255f, 3f / 255f));
        renderer.drawBox3D(new Vector3f(t2.position).add(bc2.getOffset()), bc2.getSize(), 0.0f, new Vector3f(252f / 255f, 248f / 255f, 3f / 255f));
         */
        renderer.flush(camera);
    }

    @Override
    public void processInput(float dt) {
        if (isKeyBeginPress(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setShouldClose(true);
        }
        if (isKeyPressed(GLFW_KEY_W)) {
            camera.handleKeyboard(Camera.Movement.FORWARD, dt);
        }
        if (isKeyPressed(GLFW_KEY_S)) {
            camera.handleKeyboard(Camera.Movement.BACKWARD, dt);
        }
        if (isKeyPressed(GLFW_KEY_A)) {
            camera.handleKeyboard(Camera.Movement.LEFT, dt);
        }
        if (isKeyPressed(GLFW_KEY_D)) {
            camera.handleKeyboard(Camera.Movement.RIGHT, dt);
        }
        if (isKeyPressed(GLFW_KEY_SPACE)) {
            camera.handleKeyboard(Camera.Movement.UP, dt);
        }
        if (isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            camera.handleKeyboard(Camera.Movement.DOWN, dt);
        }
        if (isKeyPressed(GLFW_KEY_COMMA)) {
            camera.setPosition(cameraPos);
            camera.setYaw(yaw);
            camera.setPitch(pitch);
            camera.setZoom(zoom);
            camera.updateCameraVectors();
        }
        float val = 0.05f;
        if (isKeyPressed(GLFW_KEY_UP)) {
            position.z -= val;
        }
        if (isKeyPressed(GLFW_KEY_DOWN)) {
            position.z += val;
        }
        if (isKeyPressed(GLFW_KEY_LEFT)) {
            position.x -= val;
        }
        if (isKeyPressed(GLFW_KEY_RIGHT)) {
            position.x += val;
        }
        if (isKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
            position.y -= val;
        }
        if (isKeyPressed(GLFW_KEY_KP_ADD)) {
            position.y += val;
        }
    }

    @Override
    public void destroy() {
        renderer.destroy();
    }
}
