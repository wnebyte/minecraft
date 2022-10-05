package com.github.wnebyte.minecraft.core;

import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Camera {

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

    public static final float DEFAULT_PROJECTION_WIDTH = 6;

    public static final float DEFAULT_PROJECTION_HEIGHT = 3;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private Vector3f position;

    private Vector3f front;

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

    private Movement lastMovement;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Camera(Vector3f position, Vector3f front, Vector3f up) {
        this(position, front, up, DEFAULT_YAW, DEFAULT_PITCH);
    }

    public Camera(Vector3f position, Vector3f front, Vector3f up, float yaw, float pitch) {
        this(position, front, up,
                yaw, pitch, DEFAULT_MOVEMENT_SPEED, DEFAULT_MOUSE_SENSITIVITY, DEFAULT_ZOOM);
    }

    public Camera(Vector3f position, Vector3f front, Vector3f up,
                  float yaw, float pitch, float movementSpeed, float mouseSensitivity, float zoom) {
        this(position, front, up,
                yaw, pitch, movementSpeed, mouseSensitivity, zoom, DEFAULT_Z_NEAR, DEFAULT_Z_FAR);
    }

    public Camera(Vector3f position, Vector3f front, Vector3f up,
                  float yaw, float pitch, float movementSpeed, float mouseSensitivity, float zoom,
                  float zNear, float zFar) {
        this.position = position;
        this.front = front;
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
        updateCameraVectors();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public Matrix4f getProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(
                (float)Math.toRadians(zoom),
                (float)Application.getWindow().getWidth() / (float)Application.getWindow().getHeight(), zNear, zFar);
        projectionMatrix.invert(inverseProjection);
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.identity();
        viewMatrix.lookAt(position, new Vector3f(position).add(front), up);
        viewMatrix.invert(inverseView);
        return viewMatrix;
    }

    /**
     * Processes input received from any keyboard-like input system.
     */
    public void handleKeyboard(Movement direction, float dt) {
        float velocity = movementSpeed * dt;
        switch (direction) {
            case FORWARD:
                position.add(new Vector3f(front).mul(velocity));
                break;
            case BACKWARD:
                position.sub(new Vector3f(front).mul(velocity));
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
        lastMovement = direction;
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
        f.normalize(front);
        // calculate the new right vector
        Vector3f r = new Vector3f(right);
        front.cross(worldUp, r);
        r.normalize(right);
        // calculate the new up vector
        Vector3f u = new Vector3f(up);
        right.cross(front, u);
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

    public Vector3f getFront() {
        return front;
    }

    public float getZNear() {
        return zNear;
    }

    public float getZFar() {
        return zFar;
    }

    public Movement getLastMovement() {
        return lastMovement;
    }
}
