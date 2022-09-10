package com.github.wnebyte.minecraft.core;

import java.util.List;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.light.Caster;

public class Scene {

    private List<GameObject> gameObjects;

    private List<Caster> lightCasters;

    public Scene() {
        this.gameObjects = new ArrayList<>();
        this.lightCasters = new ArrayList<>();
    }

    public void start() {}

    public void update() {}

    public void render() {}

    public void destory() {}
}
