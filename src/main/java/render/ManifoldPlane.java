package render;

import manifold.Manifold;
import manifold.Vec3;

/**
 * A geodesic plane in a curved manifold.
 * 
 * In flat space, this is just a regular plane.
 * In S³, this is a great 2-sphere (a totally geodesic submanifold).
 * In H³, this is a geodesic plane (also totally geodesic).
 * 
 * For simplicity, we define the plane by a reference point and a normal vector.
 * A point is "on" the plane if moving from the reference point to that point
 * along a geodesic, the tangent vector is orthogonal to the normal.
 * 
 * @param <P> The manifold's point type
 */
public class ManifoldPlane<P> {

    private final Manifold<P> manifold;
    private final P referencePoint; // A point on the plane
    private final Vec3 normal; // Normal direction (in tangent space at reference)
    private final double tileSize;
    private final Vec3 color1;
    private final Vec3 color2;

    public ManifoldPlane(Manifold<P> manifold, P referencePoint, Vec3 normal,
            double tileSize, Vec3 color1, Vec3 color2) {
        this.manifold = manifold;
        this.referencePoint = referencePoint;
        this.normal = normal.normalize();
        this.tileSize = tileSize;
        this.color1 = color1;
        this.color2 = color2;
    }

    /**
     * Compute the signed distance from a point to this plane.
     * Positive above the plane (in normal direction), negative below.
     */
    public double signedDistance(P point) {
        Vec3 toPoint = manifold.log(referencePoint, point);
        return toPoint.dot(normal);
    }

    /**
     * Check if a geodesic from prevPoint to nextPoint crosses this plane.
     * Returns the interpolation parameter t (0 to 1) if it crosses, or -1 if not.
     */
    public double crossingParameter(double prevSignedDist, double nextSignedDist) {
        // Crossing occurs when signs differ
        if (prevSignedDist > 0 && nextSignedDist <= 0) {
            return prevSignedDist / (prevSignedDist - nextSignedDist);
        }
        if (prevSignedDist < 0 && nextSignedDist >= 0) {
            return -prevSignedDist / (nextSignedDist - prevSignedDist);
        }
        return -1;
    }

    /**
     * Get the checkerboard color at a point on the plane.
     * Uses the R³ projection of the point for stable coordinates.
     */
    public Vec3 getColor(P point) {
        // Use projected R³ coordinates for checkerboard pattern
        // This is more stable than using the log map
        Vec3 proj = manifold.projectToR3(point);

        // Use x and z coordinates for horizontal checkerboard (y is up)
        int cu = (int) Math.floor(proj.x / tileSize);
        int cv = (int) Math.floor(proj.z / tileSize);

        boolean even = ((cu + cv) % 2 == 0);
        return even ? color1 : color2;
    }

    public Vec3 getNormal() {
        return normal;
    }

    public P getReferencePoint() {
        return referencePoint;
    }
}
