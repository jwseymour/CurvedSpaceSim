package sim.surface;

import sim.math.Vector2;
import sim.math.Vector3;

public interface ParametricSurface {

    // Returns 3D world position for parametric coordinates (u,v)
    Vector3 position(double u, double v);

    // Returns tangent vectors at (u,v)
    Vector3[] tangentVectors(double u, double v);

    // Move along parametric velocity in (u,v)
    Vector2 moveParam(double u, double v, Vector2 velocityUV, double dt);

    // Optional: curvature at (u,v)
    double curvature(double u, double v);
}
