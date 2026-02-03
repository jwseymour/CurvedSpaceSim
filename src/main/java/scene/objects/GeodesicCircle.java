package scene.objects;

import geometry.Space2D;
import geometry.SphericalSpace2D;
import geometry.EuclideanSpace2D;
import rays.Ray2D;
import manifold.Vec3;

public class GeodesicCircle implements SceneObject2D {
    private final Vec3 center;
    private final double radius;
    private final Vec3 color;

    public GeodesicCircle(Vec3 center, double radius, Vec3 color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
    }

    public Vec3 getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public double intersect(Ray2D ray, Space2D space) {
        if (space instanceof SphericalSpace2D) {
            return intersectSpherical(ray, (SphericalSpace2D) space);
        } else if (space instanceof EuclideanSpace2D) {
            return intersectEuclidean(ray);
        }
        return Double.POSITIVE_INFINITY;
    }

    private double intersectSpherical(Ray2D ray, SphericalSpace2D space) {
        double R = space.getRadius();

        // Equation: A cos(t/R) + B sin(t/R) = C
        double A = ray.p.dot(center);
        double B = ray.v.dot(center) * R;
        double C = R * R * Math.cos(radius / R);

        double mag = Math.sqrt(A * A + B * B);
        if (Math.abs(C) > mag) {
            return Double.POSITIVE_INFINITY; // No intersection
        }

        // cos(theta - alpha) = C/mag
        double alpha = Math.atan2(B, A);
        double delta = Math.acos(C / mag);

        // Two solutions: theta = alpha +/- delta
        double theta1 = alpha - delta;
        double theta2 = alpha + delta;

        // Normalize to positive range to find smallest positive t?
        // t = theta * R.
        // We want smallest POSITIVE t.

        // Ray starts at t=0.
        // We need to handle periodicity if we loop around the sphere?
        // A ray can intersect a circle multiple times on a sphere (entry, exit, then
        // loop around).
        // Standard ray tracer usually wants closest positive.
        // But typically we check immediate hits.

        // Let's formulate t1, t2.
        double t1 = theta1 * R;
        double t2 = theta2 * R;

        // Adjust to be > some epsilon?
        double eps = 1e-6;

        // If t is negative, add full circle period 2*pi*R?
        // On sphere, intersections recur every 2*pi*R.
        double period = 2 * Math.PI * R;

        while (t1 < eps)
            t1 += period;
        while (t2 < eps)
            t2 += period;

        // We want the smallest of valid future intersections?
        // Standard behavior: closest intersection.
        // We can just take min(t1, t2).

        return Math.min(t1, t2);
    }

    private double intersectEuclidean(Ray2D ray) {
        // |p + vt - c|^2 = r^2
        Vec3 d = ray.p.sub(center);

        // Quadratic: at^2 + bt + c = 0
        // a = v.v
        double a = ray.v.dot(ray.v); // Should be 1 if normalized
        double b = 2 * d.dot(ray.v);
        double c = d.dot(d) - radius * radius;

        double discrim = b * b - 4 * a * c;
        if (discrim < 0)
            return Double.POSITIVE_INFINITY;

        double sqrtDiscrim = Math.sqrt(discrim);
        double t1 = (-b - sqrtDiscrim) / (2 * a);
        double t2 = (-b + sqrtDiscrim) / (2 * a);

        double eps = 1e-6;
        if (t1 > eps)
            return t1;
        if (t2 > eps)
            return t2;

        return Double.POSITIVE_INFINITY;
    }

    @Override
    public Vec3 getColor() {
        return color;
    }

    @Override
    public Vec3 getPosition() {
        return center;
    }
}
