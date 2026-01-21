package render;

import manifold.Manifold;
import manifold.Vec3;

/**
 * A sphere that exists natively in a curved manifold.
 * Instead of being at a fixed R³ position, it's at a specific manifold point.
 * 
 * This allows proper wrapping in closed manifolds like S³ - a ray can
 * hit the same sphere multiple times as it goes around the great circle.
 * 
 * @param <P> The manifold's point type
 */
public class ManifoldSphere<P> {

    private final Manifold<P> manifold;
    private final P center; // Position in the manifold
    private final double radius; // Geodesic radius
    private final Vec3 color;

    public ManifoldSphere(Manifold<P> manifold, P center, double radius, Vec3 color) {
        this.manifold = manifold;
        this.center = center;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Check if a point in the manifold is inside this sphere.
     * Uses geodesic distance, so this works correctly in curved space.
     */
    public boolean contains(P point) {
        double dist = manifold.distance(center, point);
        return dist < radius;
    }

    /**
     * Get the geodesic distance from a point to the sphere's surface.
     * Negative if inside the sphere.
     */
    public double distanceToSurface(P point) {
        return manifold.distance(center, point) - radius;
    }

    public P getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Vec3 getColor() {
        return color;
    }

    /**
     * Get the outward normal at a point on or near the sphere's surface.
     * Returns the unit tangent vector from center toward the point.
     */
    public Vec3 getNormal(P point) {
        Vec3 toPoint = manifold.log(center, point);
        double norm = toPoint.norm();
        if (norm < 1e-10) {
            return Vec3.Y; // Arbitrary direction if at center
        }
        return toPoint.normalize();
    }
}
