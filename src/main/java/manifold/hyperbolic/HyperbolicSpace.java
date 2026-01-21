package manifold.hyperbolic;

import manifold.Manifold;
import manifold.Vec3;
import manifold.Vec4;

/**
 * Hyperbolic 3-space (H³) using the hyperboloid model.
 * 
 * The hyperboloid is: {(t,x,y,z) ∈ R^{1,3} : -t² + x² + y² + z² = -R², t > 0}
 * 
 * Key formulas (all derived from the Minkowski inner product):
 * 
 * Distance: cosh(d/R) = -⟨p, q⟩_M / R²
 * 
 * Exponential map: exp_p(v) = cosh(|v|/R)·p + R·sinh(|v|/R)·v̂
 * where v̂ is the unit tangent vector
 * 
 * Tangent vectors at p satisfy: ⟨p, v⟩_M = 0
 * 
 * Parallel transport from p to q:
 * v' = v + ⟨q, v⟩_M / (R² + ⟨p, q⟩_M) · (p + q)
 */
public class HyperbolicSpace implements Manifold<HyperboloidPoint> {

    private final double R; // Curvature radius (K = -1/R²)

    public HyperbolicSpace(double R) {
        if (R <= 0)
            throw new IllegalArgumentException("Radius must be positive");
        this.R = R;
    }

    /**
     * Create H³ with curvature K = -1.
     */
    public static HyperbolicSpace unit() {
        return new HyperbolicSpace(1.0);
    }

    public double getRadius() {
        return R;
    }

    @Override
    public double curvature() {
        return -1.0 / (R * R);
    }

    /**
     * Exponential map on H³.
     * 
     * Given point p and tangent vector v (in R³ representation),
     * returns the point at geodesic distance |v| from p in direction v.
     * 
     * Formula: exp_p(v) = cosh(d/R)·p + R·sinh(d/R)·v̂
     * where d = |v| and v̂ is the unit tangent vector lifted to R^{1,3}.
     */
    @Override
    public HyperboloidPoint exp(HyperboloidPoint p, Vec3 v) {
        double d = v.norm();
        if (d < 1e-12) {
            return p; // No movement
        }

        // Lift tangent vector to Minkowski space (tangent at p)
        Vec4 vLifted = liftTangent(p, v);

        // Compute geodesic
        double coshD = Math.cosh(d / R);
        double sinhD = Math.sinh(d / R);

        Vec4 result = p.coords.scale(coshD).add(vLifted.scale(R * sinhD / d));

        // Normalize to correct for numerical error
        return HyperboloidPoint.normalize(result, R);
    }

    /**
     * Logarithm map on H³.
     * 
     * Returns the tangent vector at p pointing toward q, with magnitude
     * equal to the geodesic distance.
     */
    @Override
    public Vec3 log(HyperboloidPoint p, HyperboloidPoint q) {
        double d = distance(p, q);
        if (d < 1e-12) {
            return Vec3.ZERO;
        }

        // The direction is given by (q - cosh(d/R)·p) / sinh(d/R)
        double coshD = Math.cosh(d / R);
        double sinhD = Math.sinh(d / R);

        Vec4 direction = q.coords.sub(p.coords.scale(coshD)).scale(1.0 / (R * sinhD));

        // Project to R³ tangent representation and scale by distance
        return projectTangent(p, direction).scale(d);
    }

    /**
     * Geodesic distance between two points on H³.
     * 
     * Formula: cosh(d/R) = -⟨p, q⟩_M / R²
     */
    @Override
    public double distance(HyperboloidPoint p, HyperboloidPoint q) {
        double minkProduct = p.coords.minkowski(q.coords);
        // Clamp to avoid numerical issues at very small distances
        double coshD = Math.max(1.0, -minkProduct / (R * R));
        return R * acosh(coshD);
    }

    /**
     * Parallel transport a tangent vector from p to q along the geodesic.
     * 
     * Formula: v' = v - ⟨q, v⟩_M · (p + q) / (R² + ⟨p, q⟩_M)
     */
    @Override
    public Vec3 parallelTransport(Vec3 v, HyperboloidPoint from, HyperboloidPoint to) {
        if (distance(from, to) < 1e-12) {
            return v; // No transport needed
        }

        Vec4 vLifted = liftTangent(from, v);

        double pq = from.coords.minkowski(to.coords);
        double qv = to.coords.minkowski(vLifted);
        double denom = R * R + pq;

        if (Math.abs(denom) < 1e-12) {
            // Points are antipodal (in limiting sense); this shouldn't happen in H³
            return v;
        }

        Vec4 sum = from.coords.add(to.coords);
        Vec4 transported = vLifted.sub(sum.scale(qv / denom));

        return projectTangent(to, transported);
    }

    @Override
    public double innerProduct(HyperboloidPoint p, Vec3 v1, Vec3 v2) {
        // The metric on H³ is induced from Minkowski space, but for tangent vectors
        // orthogonal to p, this coincides with the Euclidean inner product
        // on the lifted vectors. Since we represent tangents in R³, we use
        // the Euclidean product directly.
        return v1.dot(v2);
    }

    /**
     * Project a hyperboloid point to the Poincaré ball model.
     * 
     * Mapping: (t, x, y, z) → (x, y, z) / (t + R)
     * 
     * This maps the hyperboloid to the open ball of radius 1.
     */
    @Override
    public Vec3 projectToR3(HyperboloidPoint p) {
        double denom = p.t() + R;
        return new Vec3(p.x() / denom, p.y() / denom, p.z() / denom);
    }

    /**
     * Convert Poincaré ball coordinates to a hyperboloid point.
     * 
     * Inverse mapping: (x, y, z) → R · (1 + r², 2x, 2y, 2z) / (1 - r²)
     * where r² = x² + y² + z²
     */
    @Override
    public HyperboloidPoint pointFromR3(Vec3 v) {
        double rSq = v.normSq();
        if (rSq >= 1.0) {
            throw new IllegalArgumentException("Point outside Poincaré ball: |v| = " + Math.sqrt(rSq));
        }
        double scale = R / (1 - rSq);
        return new HyperboloidPoint(new Vec4(
                scale * (1 + rSq),
                scale * 2 * v.x,
                scale * 2 * v.y,
                scale * 2 * v.z));
    }

    @Override
    public Vec3 tangentToR3(HyperboloidPoint p, Vec3 v) {
        // For visualization, we use the differential of the Poincaré projection.
        // This scales tangent vectors near the boundary.
        // For simplicity, we return v directly (the conformal distortion is
        // handled by the ray tracer).
        return v;
    }

    // ==================== Helper methods ====================

    /**
     * Lift a tangent vector from R³ representation to Minkowski space.
     * 
     * Tangent vectors at p satisfy ⟨p, v⟩_M = 0.
     * Given an R³ vector, we embed it as (0, vx, vy, vz) and then
     * subtract the component along p to make it orthogonal.
     */
    private Vec4 liftTangent(HyperboloidPoint p, Vec3 v) {
        // Start with spatial embedding
        Vec4 spatial = new Vec4(0, v.x, v.y, v.z);

        // Make orthogonal to p in Minkowski sense
        // We need: ⟨p, result⟩_M = 0
        // result = spatial + λ·p
        // ⟨p, spatial + λ·p⟩_M = ⟨p, spatial⟩_M + λ·⟨p, p⟩_M = 0
        // ⟨p, spatial⟩_M = p.x·v.x + p.y·v.y + p.z·v.z (since spatial.t = 0)
        // ⟨p, p⟩_M = -R² (by definition of hyperboloid)
        // λ = ⟨p, spatial⟩_M / R²

        double pDotSpatial = p.x() * v.x + p.y() * v.y + p.z() * v.z;
        double lambda = pDotSpatial / (R * R);

        return spatial.add(p.coords.scale(lambda));
    }

    /**
     * Project a Minkowski tangent vector to R³ representation.
     * 
     * This extracts the spatial components and adjusts for the embedding.
     */
    private Vec3 projectTangent(HyperboloidPoint p, Vec4 vLifted) {
        // The R³ representation is just the spatial part
        return vLifted.spatial();
    }

    /**
     * Inverse hyperbolic cosine.
     */
    private static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Get the origin of this hyperbolic space.
     */
    public HyperboloidPoint origin() {
        return HyperboloidPoint.origin(R);
    }
}
