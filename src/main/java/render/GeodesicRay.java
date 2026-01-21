package render;

import manifold.Vec3;

/**
 * A ray in curved space, represented by its current position (in R³
 * visualization)
 * and direction (tangent vector).
 * 
 * For the ray tracer, we work in the projected R³ coordinates for intersection
 * testing and visualization, but the geodesic stepping happens in the
 * manifold's
 * native representation.
 */
public class GeodesicRay {

    /** Current position in R³ (Poincaré ball for H³, stereographic for S³) */
    public final Vec3 origin;

    /** Direction as a unit tangent vector */
    public final Vec3 direction;

    /** Total distance traveled along this ray */
    public final double traveled;

    public GeodesicRay(Vec3 origin, Vec3 direction) {
        this(origin, direction, 0);
    }

    public GeodesicRay(Vec3 origin, Vec3 direction, double traveled) {
        this.origin = origin;
        this.direction = direction.normalize();
        this.traveled = traveled;
    }

    /**
     * Get a point along the ray at parameter t (in flat space approximation).
     * This is used for intersection testing with scene objects.
     */
    public Vec3 at(double t) {
        return origin.add(direction.scale(t));
    }

    /**
     * Create a new ray with updated position and accumulated distance.
     */
    public GeodesicRay advance(Vec3 newOrigin, Vec3 newDirection, double stepSize) {
        return new GeodesicRay(newOrigin, newDirection, traveled + stepSize);
    }

    @Override
    public String toString() {
        return String.format("Ray(origin=%s, dir=%s, traveled=%.4f)", origin, direction, traveled);
    }
}
