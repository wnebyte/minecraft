package com.github.wnebyte.minecraft.physics;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.util.JMath;

public class Physics {

    private Renderer renderer;

    private Map map;

    private Vector3f gravity = new Vector3f(0.0f, 20.0f, 0.0f);

    private float physicsTime = 0.0f;

    private final float physicsTimeStep = 1.0f / 60.0f;

    private final int velocityIterations = 8;

    private final int positionIterations = 3;

    private World world;

    public Physics(Map map) {
        this.renderer = Renderer.getInstance();
        this.map = map;
        this.world = new World(gravity, map);
    }

    public void start() {

    }

    public void update(float dt) {
        physicsTime += dt;
        if (physicsTime >= 0.0f) {
            physicsTime -= physicsTimeStep;
            world.step(physicsTimeStep, velocityIterations, positionIterations);
        }
    }

    public void add(GameObject go) {
        if (go.getComponent(Rigidbody.class) != null && go.getComponent(Transform.class) != null) {
            world.add(go);
        }
    }

    public RaycastInfo raycast(Vector3f origin, Vector3f normal, float maxDistance) {
        RaycastInfo res = new RaycastInfo();
        res.hit = false;

        if (normal == null || normal.equals(0f, 0f, 0f)) {
            return res;
        }

        Vector3f rayEnd = JMath.add(origin, new Vector3f(normal).mul(maxDistance));
        // http://www.cse.yorku.ca/~amana/research/grid.pdf
        // Do some fancy math to figure out which voxel is the next voxel
        Vector3f blockCenter = JMath.ceil(origin);
        Vector3f step        = JMath.sign(normal);
        // Max amount we can step in any direction of the ray, and remain in the voxel
        Vector3f blockCenterToOriginSign = JMath.sign(JMath.sub(blockCenter, origin));
        normal.x = (normal.x == 0.0f) ? (float)(1E-10 * blockCenterToOriginSign.x) : normal.x;
        normal.y = (normal.y == 0.0f) ? (float)(1E-10 * blockCenterToOriginSign.y) : normal.y;
        normal.z = (normal.z == 0.0f) ? (float)(1E-10 * blockCenterToOriginSign.z) : normal.z;
        Vector3f tDelta = JMath.div(JMath.sub(JMath.add(blockCenter, step), origin), normal);
        // If any number is 0, then we max the delta, so we don't get a false positive
        if (tDelta.x == 0.0f) tDelta.x = (float)1E10;
        if (tDelta.y == 0.0f) tDelta.y = (float)1E10;
        if (tDelta.z == 0.0f) tDelta.z = (float)1E10;
        Vector3f tMax;
        float minTValue;

        do {
            tDelta = JMath.div(JMath.sub(blockCenter, origin), normal);
            tMax = tDelta;

            if (tMax.x < tMax.y) {
                if (tMax.x < tMax.z) {
                    blockCenter.x += step.x;
                    // check if we actually hit the block
                    if (doRaycast(origin, normal, maxDistance, blockCenter, step, res)) {
                        return res;
                    }
                    minTValue = tMax.x;
                } else {
                    blockCenter.z += step.z;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, step, res)) {
                        return res;
                    }
                    minTValue = tMax.z;
                }
            } else {
                if (tMax.y < tMax.z) {
                    blockCenter.y += step.y;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, step, res)) {
                        return res;
                    }
                    minTValue = tMax.y;
                } else {
                    blockCenter.z += step.z;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, step, res)) {
                        return res;
                    }
                    minTValue = tMax.z;
                }
            }
        } while (minTValue < maxDistance);

        return res;
    }

    private boolean doRaycast(Vector3f origin, Vector3f normal, float maxDistance, Vector3f blockCenter, Vector3f step,
                             RaycastInfo out) {
        Block b = map.getBlock(blockCenter);
        if (!Block.isAir(b)) {
            BoxCollider box = new BoxCollider();
            box.setOffset(new Vector3f());
            box.setSize(new Vector3f(1.0f, 1.0f, 1.0f));
            Transform transform = new Transform(blockCenter);

            if (b.isSolid()) {
                Vector3f min = JMath.sub(transform.position, JMath.add(JMath.mul(box.getSize(), 0.5f), box.getOffset()));
                Vector3f max = JMath.add(transform.position, JMath.add(JMath.mul(box.getSize(), 0.5f), box.getOffset()));

                float t1 = (min.x - origin.x) / (JMath.compare(normal.x, 0.0f) ? 0.00001f : normal.x);
                float t2 = (max.x - origin.x) / (JMath.compare(normal.x, 0.0f) ? 0.00001f : normal.x);
                float t3 = (min.y - origin.y) / (JMath.compare(normal.y, 0.0f) ? 0.00001f : normal.y);
                float t4 = (max.y - origin.y) / (JMath.compare(normal.y, 0.0f) ? 0.00001f : normal.y);
                float t5 = (min.z - origin.z) / (JMath.compare(normal.z, 0.0f) ? 0.00001f : normal.z);
                float t6 = (max.z - origin.z) / (JMath.compare(normal.z, 0.0f) ? 0.00001f : normal.z);
                float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
                float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

                if (tmax < 0 || tmin > tmax) {
                    // no intersection
                    return false;
                }

                float depth;
                if (tmin < 0.0f) {
                    // The ray's origin is inside the AABB
                    depth = tmax;
                }
                else {
                    depth = tmin;
                }

                out.hit = true;
                out.point = JMath.add(origin, JMath.mul(normal, depth));
                out.center = JMath.add(transform.position, box.getOffset());
                out.size = box.getSize();
                out.contactNormal = JMath.sub(out.point, out.center);
                float maxComponent = JMath.absMax(out.contactNormal);
                if (maxComponent == Math.abs(out.contactNormal.x)) {
                    out.contactNormal = JMath.mul(new Vector3f(1, 0, 0), Math.signum(out.contactNormal.x));
                } else if (maxComponent == Math.abs(out.contactNormal.y)) {
                    out.contactNormal = JMath.mul(new Vector3f(0, 1, 0), Math.signum(out.contactNormal.y));
                } else if (maxComponent == Math.abs(out.contactNormal.z)) {
                    out.contactNormal = JMath.mul(new Vector3f(0, 0, 1), Math.signum(out.contactNormal.z));
                }
                return true;
            }

        }

        return false;
    }
}
