package com.github.wnebyte.minecraft.physics;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.util.JMath;

public class World {

    private Vector3f gravity;

    private Vector3f terminalVelocity = new Vector3f(50.0f, 50.0f, 50.0f);

    private Map map;

    private List<GameObject> gameObjects;

    public World(Vector3f gravity, Map map) {
        this.gravity = gravity;
        this.map = map;
        this.gameObjects = new ArrayList<>();
    }

    public void add(GameObject go) {
        gameObjects.add(go);
    }

    public void step(float dt, int velocityIterations, int positionIterations) {
        for (GameObject go : gameObjects) {
            Rigidbody rb = go.getComponent(Rigidbody.class);
            BoxCollider bc = go.getComponent(BoxCollider.class);
            Transform transform = go.getComponent(Transform.class);
            Vector3f velocity = rb.getVelocity();
            Vector3f acceleration = rb.getAcceleration();

            transform.position.x += velocity.x * dt;
            transform.position.y += velocity.y * dt;
            transform.position.z += velocity.z * dt;
            velocity.x += acceleration.x * dt;
            velocity.y += acceleration.y * dt;
            velocity.z += acceleration.z * dt;
            if (!rb.isSensor()) {
                velocity.x -= gravity.x * dt;
                velocity.y -= gravity.y * dt;
                velocity.z -= gravity.z * dt;
            }
            velocity.x = JMath.clamp(velocity.x, -terminalVelocity.x, terminalVelocity.x);
            velocity.y = JMath.clamp(velocity.y, -terminalVelocity.y, terminalVelocity.y);
            velocity.z = JMath.clamp(velocity.z, -terminalVelocity.z, terminalVelocity.z);

            resolveCollision(transform, rb, bc);
        }
    }

    public RaycastInfo raycast(Vector3f point1, Vector3f point2) {
        return null;
    }

    private RaycastInfo doRaycast(Vector3f point1, Vector3f point2, Vector3f center) {
        return null;
    }

    private void resolveCollision(Transform transform, Rigidbody rb, BoxCollider bc) {
        boolean c = collision(transform.position, bc);
        System.out.println(c);
    }

    private boolean collision(Vector3f pos, BoxCollider bc) {
        Vector3f aabb2 = JMath.ceil(pos);
        Block b = map.getBlock(aabb2);

        if (b != null && b.isSolid()) {
            BoxCollider box = new BoxCollider();
            box.setOffset(new Vector3f());
            box.setSize(new Vector3f(1f, 1f, 1f));
            Transform transform = new Transform(aabb2);

            Vector3f aabb1Min = JMath.sub(pos,
                    JMath.add(JMath.mul(bc.getSize(), 0.5f), bc.getOffset()));
            Vector3f aabb1Max = JMath.add(pos,
                    JMath.add(JMath.mul(bc.getSize(), 0.5f), bc.getOffset()));
            Vector3f aabb2Min = JMath.sub(transform.position,
                    JMath.add(JMath.mul(box.getSize(), 0.5f), box.getOffset()));
            Vector3f aabb2Max = JMath.add(transform.position,
                    JMath.add(JMath.mul(box.getSize(), 0.5f), box.getOffset()));
            return (aabb1Max.x > aabb2Min.x &&
                    (aabb1Min.x < aabb2Max.x) &&
                    (aabb1Max.y > aabb2Min.y) &&
                    (aabb1Min.y < aabb2Max.y) &&
                    (aabb1Max.z > aabb2Min.z) &&
                    (aabb2Min.z < aabb2Max.z));
        }

        return false;
    }
}
