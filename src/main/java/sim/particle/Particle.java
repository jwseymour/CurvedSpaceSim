package sim.particle;

import sim.math.Vector2;
import sim.math.Vector3;
import sim.surface.ParametricSurface;

public class Particle {

    private double u, v;
    private final Vector2 velocityUV;
    private final ParametricSurface surface;

    public Particle(ParametricSurface surface, double u0, double v0, Vector2 velocityUV) {
        this.surface = surface;
        this.u = u0;
        this.v = v0;
        this.velocityUV = velocityUV;
    }

    public void update(double dt) {
        Vector2 next = surface.moveParam(u, v, velocityUV, dt);
        u = next.x;
        v = next.y;
    }

    public Vector3 getPosition() {
        return surface.position(u, v);
    }
}
