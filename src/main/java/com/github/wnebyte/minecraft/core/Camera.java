package com.github.wnebyte.minecraft.core;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Camera extends Component {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public enum Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public static class Builder {

        private Vector3f position = new Vector3f();

        private Vector3f forward = DEFAULT_FORWARD;

        private Vector3f up = DEFAULT_UP;

        private float yaw = DEFAULT_YAW;

        private float pitch = DEFAULT_PITCH;

        private float movementSpeed = DEFAULT_MOVEMENT_SPEED;

        private float mouseSensitivity = DEFAULT_MOUSE_SENSITIVITY;

        private float zoom = DEFAULT_ZOOM;

        private float zNear = DEFAULT_Z_NEAR;

        private float zFar = DEFAULT_Z_FAR;

        public Builder setPosition(Vector3f position) {
            this.position = position;
            return this;
        }

        public Builder setPosition(float x, float y, float z) {
            this.position = new Vector3f(x, y, z);
            return this;
        }

        public Builder setForward(Vector3f forward) {
            this.forward = forward;
            return this;
        }

        public Builder setUp(Vector3f up) {
            this.up = up;
            return this;
        }

        public Builder setYaw(float yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder setPitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder setMovementSpeed(float movementSpeed) {
            this.movementSpeed = movementSpeed;
            return this;
        }

        public Builder setMouseSensitivity(float mouseSensitivity) {
            this.mouseSensitivity = mouseSensitivity;
            return this;
        }

        public Builder setZoom(float zoom) {
            this.zoom = zoom;
            return this;
        }

        public Builder setZNear(float zNear) {
            this.zNear = zNear;
            return this;
        }

        public Builder setZFar(float zFar) {
            this.zFar = zFar;
            return this;
        }

        public Camera build() {
            return new Camera(position, forward, up,
                    yaw, pitch, movementSpeed, mouseSensitivity, zoom, zNear, zFar);
        }
    }

    public static Camera copy(Camera camera) {
        Camera copy = new Camera(
                new Vector3f(camera.position),
                new Vector3f(camera.forward),
                new Vector3f(camera.up)
        );
        return copy;
    }

    public static Camera copy(Camera src, Camera dest) {
        dest.position = new Vector3f(src.position);
        dest.forward = new Vector3f(src.forward);
        dest.up = new Vector3f(src.up);
        dest.right = new Vector3f(src.right);
        return dest;
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final Vector3f DEFAULT_FORWARD        = new Vector3f(0, 0, -1);

    public static final Vector3f DEFAULT_UP             = new Vector3f(0, 1, 0);

    public static final float DEFAULT_YAW               = -90.0f;

    public static final float DEFAULT_PITCH             =  0.0f;

    public static final float DEFAULT_MOVEMENT_SPEED    =  2.5f;

    public static final float DEFAULT_MOUSE_SENSITIVITY =  0.08f;

    public static final float DEFAULT_ZOOM              =  90.0f;

    public static final float DEFAULT_Z_NEAR            =  0.1f;

    public static final float DEFAULT_Z_FAR             =  100.0f;

    private static final float MIN_PITCH                = -1.5f;

    private static final float MAX_PITCH                = 1.5f;

    private static final float PROJECTION_WIDTH         =  6;

    private static final float PROJECTION_HEIGHT        =  3;

    private static final Vector2f PROJECTION_SIZE = new Vector2f(PROJECTION_WIDTH, PROJECTION_HEIGHT);

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private Vector3f position;

    private Vector3f forward;

    private Vector3f up;

    private Vector3f worldUp;

    private Vector3f right;

    private float yaw;

    private float pitch;

    private float movementSpeed;

    private float mouseSensitivity;

    private float zoom;

    private float zNear;

    private float zFar;

    private Matrix4f projectionMatrix;

    private Matrix4f viewMatrix;

    private Matrix4f viewProjection;

    private Matrix4f inverseProjection;

    private Matrix4f inverseView;

    private Matrix4f projectionMatrixHUD;

    private Matrix4f viewMatrixHUD;

    private Matrix4f inverseProjectionHUD;

    private Matrix4f inverseViewHUD;

    private Frustrum frustrum;

    private boolean locked;

    private Vector3f offset;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Camera(Vector3f positon) {
        this(positon, new Vector3f(DEFAULT_FORWARD), new Vector3f(DEFAULT_UP));
    }

    public Camera(Vector3f position, Vector3f forward, Vector3f up) {
        this(position, forward, up,
                DEFAULT_YAW, DEFAULT_PITCH);
    }

    public Camera(Vector3f position, float yaw, float pitch) {
        this(position, new Vector3f(DEFAULT_FORWARD), new Vector3f(DEFAULT_UP),
                yaw, pitch);
    }

    public Camera(Vector3f position, Vector3f forward, Vector3f up, float yaw, float pitch) {
        this(position, forward, up,
                yaw, pitch, DEFAULT_MOVEMENT_SPEED, DEFAULT_MOUSE_SENSITIVITY, DEFAULT_ZOOM);
    }

    public Camera(Vector3f positon, float yaw, float pitch, float movementSpeed, float mouseSensitivity, float zoom) {
        this(positon, new Vector3f(DEFAULT_FORWARD), new Vector3f(DEFAULT_UP),
                yaw, pitch, movementSpeed, mouseSensitivity, zoom);
    }

    public Camera(Vector3f position, Vector3f forward, Vector3f up,
                  float yaw, float pitch, float movementSpeed, float mouseSensitivity, float zoom) {
        this(position, forward, up,
                yaw, pitch, movementSpeed, mouseSensitivity, zoom, DEFAULT_Z_NEAR, DEFAULT_Z_FAR);
    }

    public Camera(Vector3f position, Vector3f forward, Vector3f up,
                  float yaw, float pitch, float movementSpeed, float mouseSensitivity, float zoom,
                  float zNear, float zFar) {
        this.position = position;
        this.forward = forward;
        this.up = new Vector3f();
        this.worldUp = up;
        this.right = new Vector3f();
        this.yaw = yaw;
        this.pitch = pitch;
        this.movementSpeed = movementSpeed;
        this.mouseSensitivity = mouseSensitivity;
        this.zoom = zoom;
        this.zNear = zNear;
        this.zFar = zFar;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.viewProjection = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        this.projectionMatrixHUD = new Matrix4f();
        this.viewMatrixHUD = new Matrix4f();
        this.inverseProjectionHUD = new Matrix4f();
        this.inverseViewHUD = new Matrix4f();
        this.frustrum = new Frustrum();
        this.offset = new Vector3f();
        updateCameraVectors();
        update(0);
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    @Override
    public void update(float dt) {
        updatePosition();
        updateProjectionMatrix();
        updateViewMatrix();
        viewProjection = new Matrix4f(projectionMatrix).mul(viewMatrix);
        frustrum.update(viewProjection);
    }

    private void updatePosition() {
        if (gameObject != null && gameObject.transform != null) {
            position.set(new Vector3f(gameObject.transform.position).add(offset));
        }
    }

    public void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(
                (float)Math.toRadians(zoom),
                Application.getWindow().getAspectRatio(), zNear, zFar);
        projectionMatrix.invert(inverseProjection);
    }

    public void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.lookAt(position, new Vector3f(position).add(forward), up);
        viewMatrix.invert(inverseView);
    }

    public Matrix4f getProjectionMatrixHUD() {
        projectionMatrixHUD.identity();
        Vector2f halfSize = new Vector2f(PROJECTION_SIZE).div(2.0f);
        projectionMatrixHUD.ortho(-halfSize.x, halfSize.x, -halfSize.y, halfSize.y, -0.1f, 1000.0f);
        projectionMatrixHUD.invert(inverseProjectionHUD);
        return projectionMatrixHUD;
    }

    public Matrix4f getViewMatrixHUD() {
        viewMatrixHUD.identity();
        viewMatrixHUD.lookAt(
                new Vector3f(0.0f, 0.0f, 10.0f),
                new Vector3f(0.0f, 0.0f, 9.0f),
                new Vector3f(0.0f, 1.0f, 0.0f));
        viewMatrixHUD.invert(inverseViewHUD);
        return viewMatrixHUD;
    }

    // Todo: remove
    /**
     * Processes input received from any keyboard-like input system.
     */
    public void handleKeyboard(Movement direction, float dt) {
        float velocity = movementSpeed * dt;
        switch (direction) {
            case FORWARD:
                position.add(new Vector3f(forward).mul(velocity));
                break;
            case BACKWARD:
                position.sub(new Vector3f(forward).mul(velocity));
                break;
            case LEFT:
                position.sub(new Vector3f(right).mul(velocity));
                break;
            case RIGHT:
                position.add(new Vector3f(right).mul(velocity));
                break;
            case UP:
                position.add(new Vector3f(up).mul(velocity));
                break;
            case DOWN:
                position.sub(new Vector3f(up).mul(velocity));
                break;
        }
    }

    /**
     * Processes input received from a mouse input system.
     */
    public void handleMouseMovement(float xOffset, float yOffset, boolean constrainPitch) {
        if (locked) {
            return;
        }
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;
        yaw     += 1.7f * xOffset;
        pitch   += 0.1f * yOffset;

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (constrainPitch) {
            if (pitch > MAX_PITCH) {
                pitch = MAX_PITCH;
            }
            if (pitch < MIN_PITCH) {
                pitch = MIN_PITCH;
            }
        }

        // update front, right and up vectors using the updated Euler angels
        updateCameraVectors();
    }

    /**
     * Processes input received from a mouse-scroll wheel event.
     */
    public void handleMouseScroll(float yOffset) {
        if (locked) {
            return;
        }
        zoom -= yOffset;
        if (zoom < 1.0f) {
            zoom = 1.0f;
        }
        if (zoom > 90.0f) {
            zoom = 90.0f;
        }
    }

    public void updateCameraVectors() {
        // calculate the new front vector
        Vector3f f = new Vector3f();
        f.x = (float)Math.cos(Math.toRadians(yaw)) * (float)Math.cos(Math.toRadians(pitch));
        f.y = (float)Math.sin(pitch);
        f.z = (float)Math.sin(Math.toRadians(yaw)) * (float)Math.cos(Math.toRadians(pitch));
        f.normalize(forward);
        // calculate the new right vector
        Vector3f r = new Vector3f(right);
        forward.cross(worldUp, r);
        r.normalize(right);
        // calculate the new up vector
        Vector3f u = new Vector3f(up);
        right.cross(forward, u);
        u.normalize(up);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getViewProjection() {
        return viewProjection;
    }

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getInverseView() {
        return inverseView;
    }

    public Matrix4f getInverseProjectionHUD() {
        return inverseProjectionHUD;
    }

    public Matrix4f getInverseViewHUD() {
        return inverseViewHUD;
    }

    public void setZoom(float value) {
        this.zoom = value;
    }

    public void addZoom(float value) {
        this.zoom += value;
    }

    public void resetZoom() {
        this.zoom = DEFAULT_ZOOM;
    }

    public float getZoom() {
        return zoom;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Vector3f getForward() {
        return forward;
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getRight() {
        return right;
    }

    public float getZNear() {
        return zNear;
    }

    public float getZFar() {
        return zFar;
    }

    public Frustrum getFrustrum() {
        return frustrum;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public void setLocked(boolean value) {
        this.locked = value;
    }

    public boolean isLocked() {
        return locked;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setOffset(Vector3f offset) {
        this.offset.set(offset);
    }

    public Vector3f getOffset() {
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Camera)) return false;
        Camera camera = (Camera) o;
        return Objects.equals(camera.position, this.position) &&
                Objects.equals(camera.forward, this.forward) &&
                Objects.equals(camera.up, this.up) &&
                Objects.equals(camera.right, this.right);
    }

    @Override
    public int hashCode() {
        int result = 93;
        return result +
                Objects.hashCode(this.position) +
                Objects.hashCode(this.forward) +
                Objects.hashCode(this.up) +
                Objects.hashCode(this.right);
    }
}
