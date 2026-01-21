package sim.surface;

import sim.math.Vector2;
import sim.math.Vector3;

public class Torus implements ParametricSurface {

    private final double R; // major radius
    private final double r; // minor radius

    public Torus(double R, double r) {
        this.R = R;
        this.r = r;
    }

    @Override
    public Vector3 position(double u, double v) {
        // u, v ∈ [0, 2π]
        double x = (R + r * Math.cos(u)) * Math.cos(v);
        double y = (R + r * Math.cos(u)) * Math.sin(v);
        double z = r * Math.sin(u);
        return new Vector3(x, y, z);
    }

    @Override
    public Vector3[] tangentVectors(double u, double v) {
        Vector3 du = new Vector3(
                -r * Math.sin(u) * Math.cos(v),
                -r * Math.sin(u) * Math.sin(v),
                r * Math.cos(u)
        );
        Vector3 dv = new Vector3(
                -(R + r * Math.cos(u)) * Math.sin(v),
                (R + r * Math.cos(u)) * Math.cos(v),
                0
        );
        return new Vector3[]{du, dv};
    }

    @Override
    public Vector2 moveParam(double u, double v, Vector2 velocityUV, double dt) {
        double newU = (u + velocityUV.x * dt) % (2*Math.PI);
        double newV = (v + velocityUV.y * dt) % (2*Math.PI);
        if (newU < 0) newU += 2*Math.PI;
        if (newV < 0) newV += 2*Math.PI;
        return new Vector2(newU, newV);
    }

    @Override
    public double curvature(double u, double v) {
        // approximate Gaussian curvature formula for torus
        double cosU = Math.cos(u);
        return cosU / (r*(R + r*cosU));
    }
}
