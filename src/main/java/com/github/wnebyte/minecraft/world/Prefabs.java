package com.github.wnebyte.minecraft.world;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.GameObject;
import com.github.wnebyte.minecraft.core.Transform;
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
        go.addComponent(new Transform(new Vector3f(x, y, z), new Vector3f(scale, scale, scale)));
        go.transform = go.getComponent(Transform.class);
        Rigidbody rb = new Rigidbody();
        go.addComponent(rb);
        BoxCollider bc = new BoxCollider();
        bc.setSize(new Vector3f(1f, 1f, 1f));
        bc.setOffset(new Vector3f(0f, 0f, 0f));
        go.addComponent(bc);
        return go;
    }
}
