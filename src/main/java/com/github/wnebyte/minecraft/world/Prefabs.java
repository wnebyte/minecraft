package com.github.wnebyte.minecraft.world;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.Sun;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.components.PlayerController;
import com.github.wnebyte.minecraft.components.CharacterController;
import com.github.wnebyte.minecraft.physics.components.BoxCollider;
import com.github.wnebyte.minecraft.physics.components.Rigidbody;

public class Prefabs {

    public static GameObject createGameObject(String name, float x, float y, float z, float scale) {
        GameObject go = new GameObject(name);
        Transform transform = new Transform(new Vector3f(x, y, z), new Vector3f(scale, scale, scale));
        go.addComponent(transform);
        go.transform = transform;
        return go;
    }

    public static GameObject createPlayer(float x, float y, float z, float scale) {
        GameObject go = createGameObject("Player", x, y, z, scale);
        go.addComponent(new PlayerController());
        go.addComponent(new CharacterController());
        go.addComponent(new Inventory());
        Rigidbody rb = new Rigidbody();
        rb.setVelocity(new Vector3f());
        rb.setAcceleration(new Vector3f());
        go.addComponent(rb);
        BoxCollider bc = new BoxCollider();
        bc.setSize(new Vector3f(scale));
        bc.setOffset(new Vector3f(0f, 0f, 0f));
        go.addComponent(bc);
        return go;
    }

    public static GameObject createSun(float x, float y, float z, float scale) {
        GameObject go = createGameObject("Sun", x, y, z, scale);
        go.addComponent(new Sun());
        return go;
    }
}
