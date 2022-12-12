package com.github.wnebyte.minecraft.physics.components;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Component;

public class Rigidbody extends Component {

    public Vector3f velocity;

    public Vector3f acceleration;

    private boolean sensor;

    public Rigidbody() {
        this(new Vector3f(), new Vector3f());
    }

    public Rigidbody(Vector3f velocity) {
        this(velocity, new Vector3f());
    }

    public Rigidbody(Vector3f velocity, Vector3f acceleration) {
        this(velocity, acceleration, false);
    }

    public Rigidbody(Vector3f velocity, Vector3f acceleration, boolean sensor) {
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.sensor = sensor;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    public void setAcceleration(float x, float y, float z) {
        this.acceleration.set(x, y, z);
    }

    public boolean isSensor() {
        return sensor;
    }

    public void setSensor(boolean sensor) {
        this.sensor = sensor;
    }
}
