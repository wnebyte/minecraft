package com.github.wnebyte.minecraft.componenets;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class CharacterController {

    public float baseSpeed;

    public float runSpeed;

    public float movementSensitivity;

    public float jumpForce;

    public float downJumpForce;

    public Vector3f cameraOffset;

    public Vector3f movementAxis;

    public Vector2f viewAxis;

    public boolean isRunning;

    public boolean lockedToCamera;

    public boolean applyJumpForce;

    public boolean inMiddleOfJump;
}
