package rays;

import manifold.Vec3;

/**
 * A ray on a 2D manifold.
 * 
 * Stores:
 * - Origin point p (on the manifold)
 * - Tangent direction v (in the tangent space of p)
 */
public class Ray2D {
    public final Vec3 p;
    public final Vec3 v;

    public Ray2D(Vec3 p, Vec3 v) {
        this.p = p;
        this.v = v;
    }

    @Override
    public String toString() {
        return String.format("Ray2D{p=%s, v=%s}", p, v);
    }
}
