package manifold.spherical;

import manifold.Manifold;
import manifold.Vec3;
import manifold.Vec4;

/**
 * Spherical 3-space (S³) - the 3-sphere.
 * 
 * S³ is: {(w,x,y,z) ∈ R⁴ : w² + x² + y² + z² = R²}
 * 
 * Key formulas:
 * 
 * Distance: cos(d/R) = ⟨p, q⟩ / R² where ⟨·,·⟩ is Euclidean inner product
 * 
 * Exponential map: exp_p(v) = cos(|v|/R)·p + R·sin(|v|/R)·v̂
 * where v̂ is the unit tangent vector
 * 
 * Tangent vectors at p satisfy: ⟨p, v⟩ = 0 (Euclidean orthogonality)
 * 
 * Parallel transport from p to q:
 * v' = v - ⟨q, v⟩ / (R² + ⟨p, q⟩) · (p + q)
 * 
 * Note: This is the same formula as hyperbolic space, just with + instead of
 * Minkowski inner product!
 */
public class SphericalSpace implements Manifold<SpherePoint> {

    private final double R; // Radius (K = 1/R²)

    public SphericalSpace(double R) {
        if (R <= 0)
            throw new IllegalArgumentException("Radius must be positive");
        this.R = R;
    }

    /**
     * Create S³ with curvature K = 1.
     */
    public static SphericalSpace unit() {
        return new SphericalSpace(1.0);
    }

    public double getRadius() {
        return R;
    }

    @Override
    public double curvature() {
        return 1.0 / (R * R);
    }

    /**
     * Exponential map on S³.
     * 
     * Formula: exp_p(v) = cos(d/R)·p + R·sin(d/R)·v̂
     */
    @Override
    public SpherePoint exp(SpherePoint p, Vec3 v) {
        double d = v.norm();
        if (d < 1e-12) {
            return p;
        }

        Vec4 vLifted = liftTangent(p, v);

        double cosD = Math.cos(d / R);
        double sinD = Math.sin(d / R);

        Vec4 result = p.coords.scale(cosD).add(vLifted.scale(R * sinD / d));

        return SpherePoint.normalize(result, R);
    }

    /**
     * Logarithm map on S³.
     */
    @Override
    public Vec3 log(SpherePoint p, SpherePoint q) {
        double d = distance(p, q);
        if (d < 1e-12) {
            return Vec3.ZERO;
        }

        // Handle antipodal points (d ≈ πR)
        if (Math.abs(d - Math.PI * R) < 1e-10) {
            // Any direction is valid; pick one perpendicular to p
            Vec4 perp = findPerpendicular(p.coords);
            return projectTangent(p, perp).normalize().scale(d);
        }

        double cosD = Math.cos(d / R);
        double sinD = Math.sin(d / R);

        Vec4 direction = q.coords.sub(p.coords.scale(cosD)).scale(1.0 / (R * sinD));

        return projectTangent(p, direction).scale(d);
    }

    /**
     * Geodesic distance on S³.
     * 
     * Formula: d = R · arccos(⟨p, q⟩ / R²)
     */
    @Override
    public double distance(SpherePoint p, SpherePoint q) {
        double dot = p.coords.dot(q.coords);
        double cosD = Math.max(-1.0, Math.min(1.0, dot / (R * R)));
        return R * Math.acos(cosD);
    }

    /**
     * Parallel transport on S³.
     * 
     * Formula: v' = v - ⟨q, v⟩·(p + q) / (R² + ⟨p, q⟩)
     */
    @Override
    public Vec3 parallelTransport(Vec3 v, SpherePoint from, SpherePoint to) {
        if (distance(from, to) < 1e-12) {
            return v;
        }

        Vec4 vLifted = liftTangent(from, v);

        double pq = from.coords.dot(to.coords);
        double qv = to.coords.dot(vLifted);
        double denom = R * R + pq;

        // Handle near-antipodal points
        if (Math.abs(denom) < 1e-10) {
            // Parallel transport around half the sphere picks up a sign
            return v.negate();
        }

        Vec4 sum = from.coords.add(to.coords);
        Vec4 transported = vLifted.sub(sum.scale(qv / denom));

        return projectTangent(to, transported);
    }

    @Override
    public double innerProduct(SpherePoint p, Vec3 v1, Vec3 v2) {
        return v1.dot(v2);
    }

    /**
     * Project S³ point to R³ using stereographic projection.
     * 
     * Projects from the "south pole" (-R, 0, 0, 0) onto the hyperplane w = 0.
     * Mapping: (w, x, y, z) → R·(x, y, z) / (R + w)
     */
    @Override
    public Vec3 projectToR3(SpherePoint p) {
        double denom = R + p.w();
        if (denom < 1e-10) {
            // Near the south pole, projection goes to infinity
            // Return a large but finite value in the direction
            double scale = 1000.0;
            return new Vec3(p.x() * scale, p.y() * scale, p.z() * scale);
        }
        return new Vec3(
                R * p.x() / denom,
                R * p.y() / denom,
                R * p.z() / denom);
    }

    /**
     * Convert R³ point (stereographic) to S³.
     * 
     * Inverse of stereographic projection.
     */
    @Override
    public SpherePoint pointFromR3(Vec3 v) {
        double rSq = v.normSq();
        double denom = R * R + rSq;
        return new SpherePoint(new Vec4(
                R * (R * R - rSq) / denom,
                2 * R * R * v.x / denom,
                2 * R * R * v.y / denom,
                2 * R * R * v.z / denom));
    }

    @Override
    public Vec3 tangentToR3(SpherePoint p, Vec3 v) {
        return v;
    }

    // ==================== Helper methods ====================

    /**
     * Lift a tangent vector from R³ to R⁴ at point p.
     */
    private Vec4 liftTangent(SpherePoint p, Vec3 v) {
        // Embed as (0, vx, vy, vz)
        Vec4 spatial = new Vec4(0, v.x, v.y, v.z);

        // Make orthogonal to p
        // ⟨p, spatial + λ·p⟩ = ⟨p, spatial⟩ + λ·R² = 0
        // λ = -⟨p, spatial⟩ / R²
        double pDotSpatial = p.coords.dot(spatial);
        double lambda = -pDotSpatial / (R * R);

        return spatial.add(p.coords.scale(lambda));
    }

    /**
     * Project R⁴ tangent vector to R³.
     */
    private Vec3 projectTangent(SpherePoint p, Vec4 vLifted) {
        return vLifted.spatial();
    }

    /**
     * Find a unit vector perpendicular to v in R⁴.
     */
    private Vec4 findPerpendicular(Vec4 v) {
        // Choose one of the standard basis vectors that isn't parallel to v
        Vec4 candidate;
        if (Math.abs(v.t) < 0.9) {
            candidate = new Vec4(1, 0, 0, 0);
        } else {
            candidate = new Vec4(0, 1, 0, 0);
        }

        // Gram-Schmidt: subtract projection
        double dot = v.dot(candidate);
        double vNormSq = v.normSq();
        return candidate.sub(v.scale(dot / vNormSq)).normalize();
    }

    /**
     * Get the origin of this spherical space.
     */
    public SpherePoint origin() {
        return SpherePoint.origin(R);
    }
}
