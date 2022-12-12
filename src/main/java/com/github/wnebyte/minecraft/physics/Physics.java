package com.github.wnebyte.minecraft.physics;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;

public class Physics {

    private final Vector3f gravity = new Vector3f(0.0f, 15.0f, 0.0f);

    private final float physicsTimeStep = 1.0f / 60.0f;

    private final int velocityIterations = 8;

    private final int positionIterations = 3;

    private float physicsTime = 0.0f;

    private final World world;

    public Physics(Map map) {
        this.world = new World(map);
    }

    public void update(float dt) {
        /*
        physicsTime += dt;
        if (physicsTime >= 0.0f) {
            physicsTime -= physicsTimeStep;
            world.step(physicsTimeStep, velocityIterations, positionIterations);
        }
         */
        world.step(physicsTimeStep, velocityIterations, positionIterations);
    }

    public void add(GameObject go) {
        if (go.getComponent(Rigidbody.class) != null && go.getComponent(Transform.class) != null) {
            world.add(go);
        }
    }

    public void destroy(GameObject go) {
        world.destroy(go);
    }

    public RaycastInfo raycast(Vector3f origin, Vector3f normal, float maxDistance) {
        return world.raycast(origin, normal, maxDistance);
    }

    public boolean isOnGround(GameObject go, float height) {
        return isOnGround(go, height, false);
    }

    public boolean isOnGround(GameObject go, float height, boolean draw) {
        return world.isOnGround(go, height, draw);
    }

    public Vector3f getGravity() {
        return world.getGravity();
    }

    public Vector3f getTerminalVelocity() {
        return world.getTerminalVelocity();
    }
}
