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
        DOWN;
    }

    public static Camera copy(Camera camera) {
        Camera copy = new Camera(
                camera.position,
                camera.forward,
                camera.up
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

    public static final float DEFAULT_YAW               = -90.0f;

    public static final float DEFAULT_PITCH             =  0.0f;

    public static final float DEFAULT_MOVEMENT_SPEED    =  2.5f;

    public static final float DEFAULT_MOUSE_SENSITIVITY =  0.08f;

    public static final float DEFAULT_ZOOM              =  45.0f;

    public static final float DEFAULT_Z_NEAR            =  0.1f;

    public static final float DEFAULT_Z_FAR             =  100.0f;

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

    private Matrix4f inverseProjection;

    private Matrix4f inverseView;

    private Matrix4f projectionMatrixHUD;

    private Matrix4f viewMatrixHUD;

    private Matrix4f inverseProjectionHUD;

    private Matrix4f inverseViewHUD;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Camera(Vector3f position, Vector3f forward, Vector3f up) {
        this(position, forward, up, DEFAULT_YAW, DEFAULT_PITCH);
    }

    public Camera(Vector3f position, Vector3f forward, Vector3f up, float yaw, float pitch) {
        this(position, forward, up,
                yaw, pitch, DEFAULT_MOVEMENT_SPEED, DEFAULT_MOUSE_SENSITIVITY, DEFAULT_ZOOM);
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
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        this.projectionMatrixHUD = new Matrix4f();
        this.viewMatrixHUD = new Matrix4f();
        this.inverseProjectionHUD = new Matrix4f();
        this.inverseViewHUD = new Matrix4f();
        updateCameraVectors();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    // Todo: precalculate matrices when camera state changes
    public Matrix4f getProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(
                (float)Math.toRadians(zoom),
                Application.getWindow().getAspectRatio(), zNear, zFar);
        projectionMatrix.invert(inverseProjection);
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.identity();
        viewMatrix.lookAt(position, new Vector3f(position).add(forward), up);
        viewMatrix.invert(inverseView);
        return viewMatrix;
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
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;
        yaw     += 1.7f * xOffset;
        pitch   += 0.1f * yOffset;

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (constrainPitch) {
            if (pitch > 89.0f) {
                pitch = 89.0f;
            }
            if (pitch < -89.0f) {
                pitch = -89.0f;
            }
        }

        // update front, right and up vectors using the updated Euler angels
        updateCameraVectors();
    }

    /**
     * Processes input received from a mouse-scroll wheel event.
     */
    public void handleMouseScroll(float yOffset) {
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

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getInverseView() {
        return inverseView;
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
