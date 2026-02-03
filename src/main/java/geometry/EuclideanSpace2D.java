package geometry;

import manifold.Vec3;
import rays.Ray2D;

/**
 * 2D Euclidean space.
 * 
 * Embeds 2D space in 3D (Z=0 implicit or explicit).
 * Distance is linear Euclidean distance.
 * Rays travel in straight lines.
 */
public class EuclideanSpace2D implements Space2D {

    @Override
    public Vec3 projectToTangent(Vec3 p, Vec3 v) {
        // In Euclidean space, tangent space is the space itself.
        return v;
    }

    @Override
    public double distance(Vec3 p, Vec3 q) {
        return p.sub(q).norm();
    }

    @Override
    public Ray2D advance(Ray2D ray, double t) {
        // x(t) = p + v * t
        // v is constant
        return new Ray2D(ray.p.add(ray.v.scale(t)), ray.v);
    }

    @Override
    public Vec3 getOrthogonal(Vec3 p, Vec3 v) {
        return new Vec3(0, 0, 1).cross(v);
    }
}
