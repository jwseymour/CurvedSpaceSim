package render;

import manifold.Vec3;

/**
 * A sphere in 3D space (in visualization coordinates).
 */
public class Sphere3D implements SceneObject {

    private final Vec3 center;
    private final double radius;
    private final Vec3 color;

    public Sphere3D(Vec3 center, double radius, Vec3 color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
    }

    @Override
    public Hit intersect(GeodesicRay ray, double tMin, double tMax) {
        // Quadratic formula for sphere intersection
        Vec3 oc = ray.origin.sub(center);

        double a = ray.direction.dot(ray.direction);
        double halfB = oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;

        double discriminant = halfB * halfB - a * c;
        if (discriminant < 0) {
            return null;
        }

        double sqrtD = Math.sqrt(discriminant);

        // Try the nearer root first
        double t = (-halfB - sqrtD) / a;
        if (t < tMin || t > tMax) {
            t = (-halfB + sqrtD) / a;
            if (t < tMin || t > tMax) {
                return null;
            }
        }

        Vec3 hitPoint = ray.at(t);
        Vec3 normal = hitPoint.sub(center).normalize();

        return new Hit(t, hitPoint, normal, this);
    }

    @Override
    public Vec3 getColor(Vec3 point, Vec3 normal) {
        return color;
    }

    public Vec3 getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
