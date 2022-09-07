package com.github.wnebyte.minecraft.core;

import org.joml.Vector3f;
import org.joml.Matrix4f;

public class MyCamera {

    private Vector3f position;

    private Matrix4f projectionMatrix;

    private Matrix4f viewMatrix;

    private Matrix4f inverseProjection;

    private Matrix4f inverseView;

    private Vector3f projectionSize;

    private float fov;

    private float aspect;

    private float zNear;

    private float zFar;

    public MyCamera(Vector3f position, float fov, float aspect, float zNear, float zFar) {
        this.position = position;
        this.fov = fov;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspect, zNear, zFar);
        projectionMatrix.invert(inverseProjection);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void lookAt(Vector3f eye, Vector3f center, Vector3f up) {
        viewMatrix.identity();
        viewMatrix.lookAt(eye, center, up);
        viewMatrix.invert(inverseView);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getInverseView() {
        return inverseView;
    }

    public Vector3f getProjectionSize() {
        return projectionSize;
    }

    public Vector3f getPosition() {
        return position;
    }
}
