package com.github.wnebyte.minecraft.light;

import com.github.wnebyte.minecraft.core.Component;

public abstract class Caster extends Component {

    protected Light light;

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }
}
