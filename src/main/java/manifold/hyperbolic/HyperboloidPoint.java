package manifold.hyperbolic;

import manifold.Vec4;

/**
 * A point on the hyperboloid model of hyperbolic 3-space (H³).
 * 
 * Points satisfy: -t² + x² + y² + z² = -R² (with t > 0)
 * 
 * This is the upper sheet of a two-sheeted hyperboloid embedded in
 * Minkowski space R^{1,3}.
 * 
 * Advantages of this model:
 * - Isometries are linear (Lorentz transformations)
 * - Geodesics are intersections with 2-planes through origin
 * - Numerically stable everywhere
 */
public class HyperboloidPoint {

    public final Vec4 coords;

    /**
     * Create a hyperboloid point from Minkowski coordinates.
     * Assumes the point already lies on the hyperboloid.
     */
    public HyperboloidPoint(Vec4 coords) {
        this.coords = coords;
    }

    /**
     * Create a hyperboloid point, normalizing to the hyperboloid surface.
     * The input should be approximately on the hyperboloid; this corrects
     * for numerical drift.
     * 
     * If the point has drifted too far (Minkowski norm is positive/zero),
     * we reconstruct a valid point by keeping spatial direction and computing t.
     */
    public static HyperboloidPoint normalize(Vec4 v, double R) {
        // Compute Minkowski norm squared: -t² + x² + y² + z²
        // For a valid hyperboloid point, this should be -R²
        double spatialNormSq = v.x * v.x + v.y * v.y + v.z * v.z;
        double minkNormSq = -v.t * v.t + spatialNormSq;

        // If the point has positive Minkowski norm (spacelike), it's outside the light
        // cone
        // We need to reconstruct a valid point
        if (minkNormSq >= -1e-10) {
            // Reconstruct: keep spatial direction, compute t such that point is on
            // hyperboloid
            // -t² + spatialNormSq = -R²
            // t² = R² + spatialNormSq
            // t = sqrt(R² + spatialNormSq)
            double newT = Math.sqrt(R * R + spatialNormSq);
            return new HyperboloidPoint(new Vec4(newT, v.x, v.y, v.z));
        }

        // Normal case: scale to put on hyperboloid
        // s²(-t² + x² + y² + z²) = -R²
        // s² = R² / (t² - x² - y² - z²) = R² / (-minkNormSq)
        double s = R / Math.sqrt(-minkNormSq);
        return new HyperboloidPoint(v.scale(s));
    }

    /**
     * Create the origin of H³ (the "north pole" of the hyperboloid).
     */
    public static HyperboloidPoint origin(double R) {
        return new HyperboloidPoint(new Vec4(R, 0, 0, 0));
    }

    public double t() {
        return coords.t;
    }

    public double x() {
        return coords.x;
    }

    public double y() {
        return coords.y;
    }

    public double z() {
        return coords.z;
    }

    @Override
    public String toString() {
        return "H³" + coords.toString();
    }
}
