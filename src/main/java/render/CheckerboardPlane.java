package render;

import manifold.Vec3;

/**
 * An infinite plane with a checkerboard pattern.
 * 
 * This is a "flat" plane in R³ visualization coordinates. In curved space,
 * it represents a locally flat surface which curves with the geometry.
 */
public class CheckerboardPlane implements SceneObject {

    private final Vec3 point; // A point on the plane
    private final Vec3 normal; // Plane normal
    private final Vec3 color1; // First checker color
    private final Vec3 color2; // Second checker color
    private final double scale; // Size of each checker tile

    /**
     * Create a horizontal (XZ) checkerboard plane at the given Y height.
     */
    public static CheckerboardPlane horizontal(double y, double scale) {
        return new CheckerboardPlane(
                new Vec3(0, y, 0),
                Vec3.Y,
                new Vec3(0.9, 0.9, 0.9), // White
                new Vec3(0.2, 0.2, 0.2), // Dark gray
                scale);
    }

    public CheckerboardPlane(Vec3 point, Vec3 normal, Vec3 color1, Vec3 color2, double scale) {
        this.point = point;
        this.normal = normal.normalize();
        this.color1 = color1;
        this.color2 = color2;
        this.scale = scale;
    }

    @Override
    public Hit intersect(GeodesicRay ray, double tMin, double tMax) {
        // Ray-plane intersection: t = (point - origin) · normal / (direction · normal)
        double denom = ray.direction.dot(normal);

        if (Math.abs(denom) < 1e-10) {
            return null; // Ray parallel to plane
        }

        double t = point.sub(ray.origin).dot(normal) / denom;

        if (t < tMin || t > tMax) {
            return null;
        }

        Vec3 hitPoint = ray.at(t);
        Vec3 hitNormal = denom < 0 ? normal : normal.negate(); // Face toward ray

        return new Hit(t, hitPoint, hitNormal, this);
    }

    @Override
    public Vec3 getColor(Vec3 point, Vec3 normal) {
        // Determine which checker tile we're in
        // Use two axes perpendicular to the normal
        Vec3 u, v;
        if (Math.abs(this.normal.y) > 0.9) {
            // Horizontal plane: use X and Z
            u = Vec3.X;
            v = Vec3.Z;
        } else {
            // Vertical or angled plane
            u = this.normal.cross(Vec3.Y).normalize();
            v = this.normal.cross(u);
        }

        double pu = point.dot(u);
        double pv = point.dot(v);

        int cu = (int) Math.floor(pu / scale);
        int cv = (int) Math.floor(pv / scale);

        boolean even = ((cu + cv) % 2 == 0);
        return even ? color1 : color2;
    }
}
