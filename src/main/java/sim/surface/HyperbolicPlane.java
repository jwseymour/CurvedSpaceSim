package sim.surface;

import sim.math.Vector2;
import sim.math.Vector3;

public class HyperbolicPlane implements ParametricSurface {

    private final double scale;

    public HyperbolicPlane(double scale) { this.scale = scale; }

    @Override
    public Vector3 position(double r, double theta) {
        double x = scale * Math.sinh(r) * Math.cos(theta);
        double y = scale * Math.sinh(r) * Math.sin(theta);
        double z = scale * Math.cosh(r);
        return new Vector3(x, y, z);
    }

    @Override
    public Vector3[] tangentVectors(double r, double theta) {
        Vector3 dr = new Vector3(
                scale * Math.cosh(r) * Math.cos(theta),
                scale * Math.cosh(r) * Math.sin(theta),
                scale * Math.sinh(r)
        );
        Vector3 dtheta = new Vector3(
                -scale * Math.sinh(r) * Math.sin(theta),
                scale * Math.sinh(r) * Math.cos(theta),
                0
        );
        return new Vector3[]{dr, dtheta};
    }

    @Override
    public Vector2 moveParam(double r, double theta, Vector2 velocityUV, double dt) {
        double newR = r + velocityUV.x * dt;
        double newTheta = theta + velocityUV.y * dt;

        newR = Math.max(0, Math.min(3, newR)); // clamp for visualization
        newTheta = (newTheta + 2*Math.PI) % (2*Math.PI);
        return new Vector2(newR, newTheta);
    }

    @Override
    public double curvature(double r, double theta) {
        return -1.0;
    }
}
