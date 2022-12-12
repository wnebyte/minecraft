package com.github.wnebyte.minecraft.physics;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.world.Map;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.util.JColor;
import com.github.wnebyte.minecraft.util.JMath;

public class World {

    public static final Vector3f X_AXIS = new Vector3f(1, 0, 0);

    public static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);

    public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

    public static final Vector3f[] AXES = { X_AXIS, Y_AXIS, Z_AXIS };

    private static final BoxCollider DEFAULT_BOX_COLLIDER = new BoxCollider(new Vector3f(1.0f, 1.0f, 1.0f));

    private static final Vector3f DEFAULT_GRAVITY = new Vector3f(0.0f, 15.0f, 0.0f);

    private static final Vector3f DEFAULT_TERMINAL_VELOCITY = new Vector3f(50.0f, 50.0f, 50.0f);

    private final Map map;

    private final Vector3f gravity;

    private final Vector3f terminalVelocity;

    private final List<GameObject> gameObjects;

    public World(Map map) {
        this(map, DEFAULT_GRAVITY);
    }

    public World(Map map, Vector3f gravity) {
        this(map, gravity, DEFAULT_TERMINAL_VELOCITY);
    }

    public World(Map map, Vector3f gravity, Vector3f terminalVelocity) {
        this.map = map;
        this.gravity = gravity;
        this.terminalVelocity = terminalVelocity;
        this.gameObjects = new ArrayList<>();
    }

    public void add(GameObject go) {
        if (go.getComponent(Transform.class) != null &&
                go.getComponent(Rigidbody.class) != null &&
                go.getComponent(BoxCollider.class) != null) {
            gameObjects.add(go);
        }
    }

    public void destroy(GameObject go) {
        gameObjects.remove(go);
    }

    public void step(float dt, int velocityIterations, int positionIterations) {
        for (GameObject go : gameObjects) {
            Rigidbody rb = go.getComponent(Rigidbody.class);
            BoxCollider bc = go.getComponent(BoxCollider.class);
            Transform transform = go.getComponent(Transform.class);
            Vector3f velocity = rb.getVelocity();
            Vector3f acceleration = rb.getAcceleration();

            // update position
            transform.position.x += velocity.x * dt;
            transform.position.y += velocity.y * dt;
            transform.position.z += velocity.z * dt;

            // update velocity
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

            // resolve collisions
            resolveCollisions(go, transform, rb, bc);
        }
    }

    public RaycastInfo raycast(Vector3f origin, Vector3f normal, float maxDistance) {
        return raycast(origin, normal, maxDistance, false);
    }

    public RaycastInfo raycast(Vector3f origin, Vector3f normal, float maxDistance, boolean draw) {
        RaycastInfo res = new RaycastInfo();
        res.hit = false;

        if (normal == null || normal.equals(0f, 0f, 0f)) {
            return res;
        }

        Vector3f rayEnd = JMath.add(origin, new Vector3f(normal).mul(maxDistance));
        if (draw) {
            Renderer renderer = Renderer.getInstance();
            renderer.drawLine3D(origin, rayEnd, JColor.YELLOW_VEC3);
        }
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
                    if (doRaycast(origin, normal, maxDistance, blockCenter, res)) {
                        return res;
                    }
                    minTValue = tMax.x;
                } else {
                    blockCenter.z += step.z;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, res)) {
                        return res;
                    }
                    minTValue = tMax.z;
                }
            } else {
                if (tMax.y < tMax.z) {
                    blockCenter.y += step.y;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, res)) {
                        return res;
                    }
                    minTValue = tMax.y;
                } else {
                    blockCenter.z += step.z;
                    if (doRaycast(origin, normal, maxDistance, blockCenter, res)) {
                        return res;
                    }
                    minTValue = tMax.z;
                }
            }
        } while (minTValue < maxDistance);

        return res;
    }

    private boolean doRaycast(Vector3f origin, Vector3f normal, float maxDistance, Vector3f blockCenter, RaycastInfo out) {
        Block b = map.getBlock(blockCenter);

        if (!Block.isAir(b)) {
            BoxCollider box = DEFAULT_BOX_COLLIDER;
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

    private void resolveCollisions(GameObject go, Transform t1, Rigidbody rb, BoxCollider bc1) {
        Vector3f min = new Vector3f(t1.position).add(bc1.getOffset())
                .sub(new Vector3f(bc1.getSize()).mul(0.5f));
        Vector3f max = new Vector3f(t1.position).add(bc1.getOffset())
                .add(new Vector3f(bc1.getSize()).mul(0.5f));
        int leftX   = (int)Math.floor(min.x);
        int rightX  = (int)Math.ceil(max.x);
        int backZ   = (int)Math.floor(min.z);
        int frontZ  = (int)Math.ceil(max.z);
        int bottomY = (int)Math.floor(min.y);
        int topY    = (int)Math.ceil(max.y);

        for (int y = topY; y >= bottomY; y--) {
            for (int x = leftX; x <= rightX; x++) {
                for (int z = backZ; z <= frontZ; z++) {
                    Vector3f blockPos = new Vector3f(x, y, z);
                    Block block = map.getBlock(blockPos);
                    Transform t2 = new Transform(blockPos);
                    BoxCollider bc2 = DEFAULT_BOX_COLLIDER;

                    if (block != null && block.isSolid() && isColliding(t1, bc1, t2, bc2)) {
                        List<Penetration> c = getPenetrations(t1, bc1, t2, bc2);
                        Penetration penetration = c.stream().min((o1, o2) -> Float.compare(o1.abs, o2.abs)).orElse(null);
                        if (penetration != null) {
                            GameObject blockGo = createGameObject(t2, bc2);
                            Vector3f contactNormal = new Vector3f(penetration.axis).mul(Math.signum(penetration.value));
                            go.preSolve(blockGo, contactNormal);
                            if (penetration.axis.equals(X_AXIS)) {
                                t1.position.x += penetration.value;
                                rb.velocity.x = 0.0f;
                            } else if (penetration.axis.equals(Y_AXIS)) {
                                t1.position.y += penetration.value;
                                rb.velocity.y = 0.0f;
                            } else {
                                t1.position.z += penetration.value;
                                rb.velocity.z = 0.0f;
                            }
                            go.postSolve(blockGo, contactNormal);
                        }
                    }

                }
            }
        }
    }

    public boolean isOnGround(GameObject go, float height) {
        return isOnGround(go, height, false);
    }

    public boolean isOnGround(GameObject go, float height, boolean draw) {
        BoxCollider bc = go.getComponent(BoxCollider.class);
        if (bc == null) {
            return false;
        }
        Vector3f tl = new Vector3f(go.transform.position).add(bc.getOffset()).sub(new Vector3f(bc.getSize()).mul(0.5f));
        Vector3f tr = new Vector3f(tl).add(bc.getSize().x, 0, 0);
        Vector3f bl = new Vector3f(tl).add(0, 0, bc.getSize().z);
        Vector3f br = new Vector3f(bl).add(bc.getSize().x, 0, 0);
        Vector3f center = new Vector3f(tl).add(bc.getSize().x / 2.0f, 0, bc.getSize().z / 2.0f);
        Vector3f[] vertices = { tl, tr, bl, br, center };
        Vector3f normal = new Vector3f(0, -1, 0);

        for (Vector3f origin : vertices) {
            RaycastInfo info = raycast(origin, normal, height, draw);
            if (info.isHit()) {
                return true;
            }
        }

        return false;
    }

    private GameObject createGameObject(Transform transform, Component... components) {
        GameObject go = new GameObject("Local");
        go.addComponent(transform);
        go.transform = transform;
        for (Component c : components) {
            go.addComponent(c);
        }
        return go;
    }

    private boolean isColliding(Transform t1, BoxCollider bc1, Transform t2, BoxCollider bc2) {
        for (int i = 0; i < AXES.length; i++) {
            Vector3f axis = AXES[i];
            float penetration = getNormalizedPenetrationAmount(t1, bc1, t2, bc2, axis);
            if (Math.abs(penetration) == 0.0f) {
                return false;
            }
        }

        return true;
    }

    private float getPenetrationAmount(Transform t1, BoxCollider bc1, Transform t2, BoxCollider bc2, Vector3f axis) {
        Vector3f min1 = new Vector3f(t1.position).add(bc1.getOffset()).sub(new Vector3f(bc1.getSize()).mul(0.5f));
        Vector3f max1 = new Vector3f(t1.position).add(bc1.getOffset()).add(new Vector3f(bc1.getSize()).mul(0.5f));
        Vector3f min2 = new Vector3f(t2.position).add(bc2.getOffset()).sub(new Vector3f(bc2.getSize()).mul(0.5f));
        Vector3f max2 = new Vector3f(t2.position).add(bc2.getOffset()).add(new Vector3f(bc2.getSize()).mul(0.5f));

        if (axis.equals(X_AXIS)) {
            if ((min2.x <= max1.x) && (min1.x <= max2.x)) {
                // we have penetration
                return min2.x - max1.x;
            }
        } else if (axis.equals(Y_AXIS)) {
            if ((min2.y <= max1.y) && (min1.y <= max2.y)) {
                // we have penetration
                return min2.y - max1.y;
            }
        } else if (axis.equals(Z_AXIS)) {
            if ((min2.z <= max1.z) && (min1.z <= max2.z)) {
                // we have penetration
                return min2.z - max1.z;
            }
        }

        return 0.0f;
    }

    private float getNormalizedPenetrationAmount(Transform t1, BoxCollider bc1, Transform t2, BoxCollider bc2, Vector3f axis) {
        float penetration = getPenetrationAmount(t1, bc1, t2, bc2, axis);
        if (penetration == 0.0f) {
            return penetration;
        }
        float abs = Math.abs(penetration);
        Vector3f expandedSize = new Vector3f(bc1.getSize()).add(bc2.getSize());
        Vector3f halfSize = new Vector3f(expandedSize).div(2.0f);

        if (axis.equals(X_AXIS)) {
            if (abs > halfSize.x) {
                penetration = expandedSize.x - abs;
            }
        } else if (axis.equals(Y_AXIS)) {
            if (abs > halfSize.y) {
                penetration = expandedSize.y - abs;
            }
        } else if (axis.equals(Z_AXIS)) {
            if (abs > halfSize.z) {
                penetration = expandedSize.z - abs;
            }
        }

        return penetration;
    }

    private List<Penetration> getPenetrations(Transform t1, BoxCollider bc1, Transform t2, BoxCollider bc2) {
        List<Penetration> c = new ArrayList<>(3);
        for (int i = 0; i < AXES.length; i++) {
            Vector3f axis = AXES[i];
            float penetration = getNormalizedPenetrationAmount(t1, bc1, t2, bc2, axis);
            if (penetration != 0.0f) {
                c.add(new Penetration(penetration, axis));
            }
        }
        return c;
    }

    public Vector3f getGravity() {
        return gravity;
    }

    public Vector3f getTerminalVelocity() {
        return terminalVelocity;
    }
}
