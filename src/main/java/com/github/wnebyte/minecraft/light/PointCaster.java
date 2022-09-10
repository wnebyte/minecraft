package com.github.wnebyte.minecraft.light;

import org.joml.Vector3f;

/*
Attentuation:
Distance	Constant	Linear	Quadratic
7	        1.0	        0.7	    1.8
13	        1.0	        0.35	0.44
20	        1.0	        0.22	0.20
32	        1.0	        0.14	0.07
50	        1.0	        0.09	0.032
65	        1.0	        0.07	0.017
100	        1.0	        0.045	0.0075
160	        1.0	        0.027	0.0028
200	        1.0	        0.022	0.0019
325	        1.0	        0.014	0.0007
600	        1.0	        0.007	0.0002
3250	    1.0	        0.0014	0.000007
 */
public class PointCaster extends Caster {

    private Vector3f position;

    private float constant;

    private float linear;

    private float quadratic;

    public PointCaster(Vector3f position, Light light, float constant, float linear, float quadratic) {
        this.position = position;
        this.light = light;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getQuadratic() {
        return quadratic;
    }

    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}
