package com.github.wnebyte.minecraft.physics.components;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Component;

public class Rigidbody extends Component {

    private Vector3f velocity;

    private Vector3f acceleration;

    private boolean sensor;

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    public boolean isSensor() {
        return sensor;
    }

    public void setSensor(boolean sensor) {
        this.sensor = sensor;
    }
}
