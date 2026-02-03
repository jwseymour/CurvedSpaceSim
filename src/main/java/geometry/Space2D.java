package geometry;

import manifold.Vec3;
import rays.Ray2D;

/**
 * Interface for a 2D space (manifold).
 * 
 * Responsibilities:
 * - Geometric operations (distance, projection)
 * - Ray advancement (geodesics)
 */
public interface Space2D {
    /**
     * Project a vector v onto the tangent space at p.
     * Ensures the vector is valid for movement at p.
     */
    Vec3 projectToTangent(Vec3 p, Vec3 v);

    /**
     * Calculate geodesic distance between p and q.
     */
    double distance(Vec3 p, Vec3 q);

    /**
     * Advance a ray by arc length t.
     * Returns the new ray (new position and new tangent direction).
     */
    Ray2D advance(Ray2D ray, double t);

    /**
     * Get a vector orthogonal to v in the tangent space at p.
     * Used for rotation and camera frame.
     */
    Vec3 getOrthogonal(Vec3 p, Vec3 v);
}
