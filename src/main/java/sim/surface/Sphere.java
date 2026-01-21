package sim.surface;

import sim.math.Vector2;
import sim.math.Vector3;

public class Sphere implements ParametricSurface {

    private final double radius;

    public Sphere(double radius) { this.radius = radius; }

    @Override
    public Vector3 position(double u, double v) {
        double x = radius * Math.sin(u) * Math.cos(v);
        double y = radius * Math.sin(u) * Math.sin(v);
        double z = radius * Math.cos(u);
        return new Vector3(x, y, z);
    }

    @Override
    public Vector3[] tangentVectors(double u, double v) {
        Vector3 du = new Vector3(
                radius * Math.cos(u) * Math.cos(v),
                radius * Math.cos(u) * Math.sin(v),
                -radius * Math.sin(u)
        );
        Vector3 dv = new Vector3(
                -radius * Math.sin(u) * Math.sin(v),
                radius * Math.sin(u) * Math.cos(v),
                0
        );
        return new Vector3[]{du, dv};
    }

    @Override
    public Vector2 moveParam(double u, double v, Vector2 velocityUV, double dt) {
        double newU = u + velocityUV.x * dt;
        double newV = v + velocityUV.y * dt;

        // Clamp u to [0, pi] and wrap v
        newU = Math.max(0, Math.min(Math.PI, newU));
        newV = (newV + 2*Math.PI) % (2*Math.PI);

        return new Vector2(newU, newV);
    }

    @Override
    public double curvature(double u, double v) {
        return 1.0 / (radius*radius);
    }
}
