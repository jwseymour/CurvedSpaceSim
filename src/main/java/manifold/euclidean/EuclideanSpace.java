package manifold.euclidean;

import manifold.Manifold;
import manifold.Vec3;

/**
 * Euclidean 3-space (E³).
 * 
 * This is the trivial case where:
 * - Geodesics are straight lines
 * - Parallel transport is identity (vectors don't change)
 * - Curvature is zero
 * 
 * Points are represented directly as Vec3.
 */
public class EuclideanSpace implements Manifold<Vec3> {

    public static final EuclideanSpace INSTANCE = new EuclideanSpace();

    private EuclideanSpace() {
    }

    @Override
    public Vec3 exp(Vec3 p, Vec3 v) {
        // In Euclidean space, exp is just addition: walk from p in direction v
        return p.add(v);
    }

    @Override
    public Vec3 log(Vec3 p, Vec3 q) {
        // The tangent vector from p to q is just the difference
        return q.sub(p);
    }

    @Override
    public double distance(Vec3 p, Vec3 q) {
        return p.sub(q).norm();
    }

    @Override
    public Vec3 parallelTransport(Vec3 v, Vec3 from, Vec3 to) {
        // In Euclidean space, parallel transport is trivial: vectors don't change
        return v;
    }

    @Override
    public double innerProduct(Vec3 p, Vec3 v1, Vec3 v2) {
        // Standard Euclidean dot product
        return v1.dot(v2);
    }

    @Override
    public double curvature() {
        return 0.0;
    }

    @Override
    public Vec3 projectToR3(Vec3 p) {
        // Identity: points are already in R³
        return p;
    }

    @Override
    public Vec3 pointFromR3(Vec3 v) {
        // Identity
        return v;
    }

    @Override
    public Vec3 tangentToR3(Vec3 p, Vec3 v) {
        // Identity: tangent vectors are already in R³
        return v;
    }
}
