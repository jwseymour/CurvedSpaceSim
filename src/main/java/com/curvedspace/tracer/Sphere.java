package com.curvedspace.tracer;

import com.curvedspace.geometry.Ray;
import com.curvedspace.math.MathUtil;
import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;

public class Sphere implements S3Object {
    private final Vec4 center;
    private final double radius; // Radius in radians
    private final Vec3 color;
    private final double cosRadius; // Cache cos(radius)

    public Sphere(Vec4 center, double radius, Vec3 color) {
        this.center = center.normalize();
        this.radius = radius;
        this.color = color;
        this.cosRadius = Math.cos(radius);
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Vec3 getColor(Vec4 point) {
        return color;
    }

    @Override
    public Intersection intersect(Ray ray) {
        // Equation: (O cos t + D sin t) . C = cos(rho)
        // (O.C) cos t + (D.C) sin t = cos(rho)
        double a = ray.origin.dot(center);
        double b = ray.dir.dot(center);

        double R = Math.sqrt(a * a + b * b);

        // Impossible to hit if max value < cos(rho)
        // Note: cos(rho) can be negative if rho > pi/2
        // Max value of a cos t + b sin t is R.
        // If cosRadius > R, then no intersection (sphere is 'above' the ray's plane?)
        if (R < Math.abs(cosRadius)) { // Use abs? No, R is always positive amplitude.
            // If cosRadius is 0.5, and R is 0.1, max value is 0.1. Can't reach 0.5.
            // If cosRadius is -0.5, and R is 0.1, min value is -0.1. Can't reach -0.5.
            if (Math.abs(cosRadius) > R)
                return null;
        }

        // sin(t + theta0) = cos(rho) / R
        // where sin(theta0) = a/R, cos(theta0) = b/R -> theta0 = atan2(a, b)
        double theta0 = Math.atan2(a, b);

        double val = cosRadius / R;
        if (Math.abs(val) > 1.0)
            return null; // Should be covered by R check but floating point safety

        double alpha = Math.asin(val);

        // t + theta0 = alpha OR t + theta0 = pi - alpha
        // t = alpha - theta0
        // t = pi - alpha - theta0

        double t1 = alpha - theta0;
        double t2 = Math.PI - alpha - theta0;

        // Normalize periods to find smallest positive t
        t1 = normalizeAngle(t1);
        t2 = normalizeAngle(t2);

        // We generally want the smallest t.
        // However, we must check if one is "entering" and one is "exiting" or both are
        // valid hits.
        // In opaque rendering, the smallest t > epsilon is the hit.

        double t = Math.min(t1, t2);
        if (t < MathUtil.EPSILON) {
            t = Math.max(t1, t2);
            if (t < MathUtil.EPSILON)
                return null; // Both behind or too close
        }

        Vec4 hitPoint = ray.getPoint(t);
        Vec4 normal = computeNormal(hitPoint);

        return new Intersection(t, hitPoint, normal, this);
    }

    // Convert angle to [0, 2pi) or similar logic to find smallest positive
    private double normalizeAngle(double t) {
        while (t < 0)
            t += 2 * Math.PI;
        while (t >= 2 * Math.PI)
            t -= 2 * Math.PI;
        return t;
    }

    private Vec4 computeNormal(Vec4 p) {
        // The normal in S^3 at p pointing away from center.
        // It is the tangent vector at p of the geodesic from center to p.
        // v_tangent = p - (p.c)c ?? No.
        // Geodesic: G(s) starting at c to p.
        // At s=radius, G(radius) = p.
        // Tangent is G'(radius).
        // If we project c onto tangent space of p?
        // tangent = normalize(p - c * (p.c) / ???)
        // Let's use S3Space.tangent(p, c) -> vector at p pointing back to c (mostly).
        // Then negate it to point away.

        // Tangent at p pointing TOWARDS c:
        // T_in = tangent(p, c) = c - p(c.p)
        // Normal (outwards) is -T_in.
        // N = p(c.p) - c

        double dot = center.dot(p);
        Vec4 n = p.scale(dot).minus(center);
        return n.normalize();
    }
}
