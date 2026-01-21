package render;

import manifold.Vec3;

/**
 * Result of a ray-object intersection.
 */
public class Hit {

    /** Distance along the ray to the hit point */
    public final double t;

    /** World position of the hit */
    public final Vec3 point;

    /** Surface normal at the hit point */
    public final Vec3 normal;

    /** The object that was hit */
    public final SceneObject object;

    public Hit(double t, Vec3 point, Vec3 normal, SceneObject object) {
        this.t = t;
        this.point = point;
        this.normal = normal;
        this.object = object;
    }

    /**
     * Check if this hit is closer than another.
     */
    public boolean closerThan(Hit other) {
        return other == null || this.t < other.t;
    }
}
