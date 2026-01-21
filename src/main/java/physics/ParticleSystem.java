package physics;

import manifold.Manifold;
import manifold.Vec3;
import render.Sphere3D;
import render.SceneObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of particles in a manifold.
 */
public class ParticleSystem<P> {

    private final Manifold<P> manifold;
    private final List<Particle<P>> particles = new ArrayList<>();

    public ParticleSystem(Manifold<P> manifold) {
        this.manifold = manifold;
    }

    public void addParticle(Particle<P> particle) {
        particles.add(particle);
    }

    public void addParticle(P position, Vec3 velocity) {
        particles.add(new Particle<>(manifold, position, velocity));
    }

    public void addParticle(P position, Vec3 velocity, Vec3 color, double radius) {
        particles.add(new Particle<>(manifold, position, velocity, color, radius));
    }

    /**
     * Update all particles.
     */
    public void update(double dt) {
        for (Particle<P> p : particles) {
            p.update(dt);
        }
    }

    /**
     * Get scene objects representing the particles (for ray tracing).
     */
    public List<SceneObject> getSceneObjects() {
        List<SceneObject> objects = new ArrayList<>();
        for (Particle<P> p : particles) {
            Vec3 posR3 = p.getPositionR3();
            objects.add(new Sphere3D(posR3, p.getRadius(), p.getColor()));
        }
        return objects;
    }

    public List<Particle<P>> getParticles() {
        return particles;
    }

    public void clear() {
        particles.clear();
    }

    public int size() {
        return particles.size();
    }
}
