package geometry;

import manifold.Vec3;
import rays.Ray2D;

/**
 * 2D Spherical space (S²) embedded in ℝ³.
 * 
 * Features:
 * - Constant positive curvature = 1/R²
 * - Geodesics are great circles
 * - Intrinsic distance is based on arc length
 */
public class SphericalSpace2D implements Space2D {
    private final double R;

    public SphericalSpace2D(double radius) {
        this.R = radius;
    }

    public double getRadius() {
        return R;
    }

    @Override
    public Vec3 projectToTangent(Vec3 p, Vec3 v) {
        // Project v onto the tangent plane at p with sphere radius R.
        return v.sub(p.scale(v.dot(p) / (R * R)));
    }

    @Override
    public double distance(Vec3 p, Vec3 q) {
        // d(p, q) = arccos( dot(p, q) / R^2 ) * R
        double dot = p.dot(q);
        double den = R * R;
        double cosVal = dot / den;

        if (cosVal > 1.0)
            cosVal = 1.0;
        else if (cosVal < -1.0)
            cosVal = -1.0;

        return Math.acos(cosVal) * R;
    }

    @Override
    public Ray2D advance(Ray2D ray, double t) {
        // Geodesic equation for sphere of radius R:
        // x(t) = p * cos(t/R) + v * R * sin(t/R)
        // x'(t) = -p/R * sin(t/R) + v * cos(t/R)

        double angle = t / R;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vec3 newP = ray.p.scale(cos).add(ray.v.scale(R * sin));
        Vec3 newV = ray.v.scale(cos).sub(ray.p.scale(sin / R));

        return new Ray2D(newP, newV);
    }

    @Override
    public Vec3 getOrthogonal(Vec3 p, Vec3 v) {
        return p.cross(v).normalize();
    }
}
