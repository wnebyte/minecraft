package com.github.wnebyte.minecraft.componenets;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.physics.RaycastInfo;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class PlayerController extends Component {

    private Camera camera;

    private Renderer renderer;

    private Physics physics;

    private Map map;

    private float destroyBlockDebounceTime = 0.2f;

    private float destroyBlockDebounce = destroyBlockDebounceTime;

    private float placeBlockDebounceTime = 0.2f;

    private float placeBlockDebounce = placeBlockDebounceTime;

    public PlayerController(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
    }

    @Override
    public void start() {
        camera = Application.getWindow().getScene().getCamera();
    }

    @Override
    public void update(float dt) {
        destroyBlockDebounce -= dt;
        placeBlockDebounce -= dt;

        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f forward = new Vector3f(camera.getForward());
        RaycastInfo info = physics.raycast(origin, forward, 15f);

        if (info.hit) {
            renderer.clearLines3D();
            renderer.addBox3D(info.blockCenter, info.blockSize, 0f,
                    new Vector3f(1f, 1f, 1f), 60 * 5);
        }

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) &&
                destroyBlockDebounce < 0) {
            destroyBlockDebounce = destroyBlockDebounceTime;
        }

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) &&
                placeBlockDebounce < 0) {
            placeBlockDebounce = placeBlockDebounceTime;
        }
    }
}
