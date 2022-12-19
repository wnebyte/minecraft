package com.github.wnebyte.minecraft.components;

import com.github.wnebyte.minecraft.renderer.Sprite;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.renderer.Cube3D;
import com.github.wnebyte.minecraft.renderer.Renderer;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyPressed;
import static org.lwjgl.glfw.GLFW.*;

public class Sun extends Component {

    private Renderer renderer;

    private final Vector3f color = new Vector3f(252f / 255f, 248f / 255f, 3f / 255f);

    private Cube3D cube;

    @Override
    public void start(Scene scene) {
        this.renderer = scene.getRenderer();
        Sprite sprite = new Sprite();
        this.cube = new Cube3D(gameObject.transform, color, sprite, sprite, sprite);
    }

    @Override
    public void update(float dt) {
        Vector3f position = gameObject.transform.position;
        float val = 1f;

        if (isKeyPressed(GLFW_KEY_LEFT)) {
            position.x -= val;
        }
        if (isKeyPressed(GLFW_KEY_RIGHT)) {
            position.x += val;
        }
        if (isKeyPressed(GLFW_KEY_UP)) {
            position.y += val;
        }
        if (isKeyPressed(GLFW_KEY_DOWN)) {
            position.y -= val;
        }
        if (isKeyPressed(GLFW_KEY_KP_ADD)) {
            position.z += val;
        }
        if (isKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
            position.z -= val;
        }

        renderer.drawCube3D(cube);
    }
}
