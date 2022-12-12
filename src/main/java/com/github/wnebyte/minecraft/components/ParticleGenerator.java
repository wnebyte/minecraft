package com.github.wnebyte.minecraft.components;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.physics.Physics;
import com.github.wnebyte.minecraft.util.JColor;
import com.github.wnebyte.minecraft.util.JMath;

public class ParticleGenerator extends Component {

    private static final float MAX_VELOCITY = 4.5f;

    private static final float MAX_Y_VELOCITY = 4.5f;

    private Renderer renderer;

    private List<Particle3D> particles;

    private Block block;

    private Physics physics;

    private int size = 20;

    private int it = 0;

    private Random rand;

    public ParticleGenerator(Block block) {
        this.particles = new ArrayList<>(size);
        this.block = block;
        this.rand = new Random();
    }

    @Override
    public void start(Scene scene) {
        this.renderer = scene.getRenderer();
        this.physics = scene.getWorld().getPhysics();
    }

    @Override
    public void update(float dt) {
        updateParticles(dt);
        if (it == 0) {
            spawnParticles();
        }
        if (particles.isEmpty()) {
            gameObject.destroy();
        }
        it++;
    }

    private void updateParticles(float dt) {
        for (int i = 0; i < particles.size(); i++) {
            Particle3D cube = particles.get(i);
            if (cube.ftl() <= 0) {
                particles.remove(i);
                i--;
            } else {
                cube.transform.position.x += cube.velocity.x * dt;
                cube.transform.position.y += cube.velocity.y * dt;
                cube.transform.position.z += cube.velocity.z * dt;
                cube.velocity.y -= physics.getGravity().y * dt;
                renderer.drawCube3D(cube);
            }
        }
    }

    private void spawnParticles() {
        for (int i = 0; i < size; i++) {
            float xVelocity = JMath.clamp(nextFloat((int)MAX_VELOCITY), -MAX_VELOCITY, MAX_VELOCITY);
            float yVelcoity = MAX_Y_VELOCITY;
            float zVelocity = JMath.clamp(nextFloat((int)MAX_VELOCITY), -MAX_VELOCITY, MAX_VELOCITY);
            Vector3f velocity = new Vector3f(xVelocity, yVelcoity, zVelocity);
            Particle3D cube = new Particle3D(new Transform(
                    new Vector3f(gameObject.transform.position).add(0f, 0.5f, 0f),
                    new Vector3f(0.2f, 0.2f, 0.2f)),
                    JColor.WHITE_VEC3,
                    block.getSideTextureFormat().getAsSprite(),
                    block.getTopTextureFormat().getAsSprite(),
                    block.getBottomTextureFormat().getAsSprite(),
                    velocity,
                    60 * 3);
            renderer.drawCube3D(cube);
            particles.add(cube);
        }
    }

    private float nextFloat(int bound) {
        float val = (float)rand.nextInt(bound + 1);
        return rand.nextBoolean() ? val : -val;
    }
}
