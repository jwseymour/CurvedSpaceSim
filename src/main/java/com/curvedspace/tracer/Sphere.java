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
        // A point is on the sphere when its dot product with the centre is
        // cos(radius). Along the ray O cos t + D sin t that gives
        //   a cos t + b sin t = cos(radius),  a = O.C,  b = D.C
        double a = ray.origin.dot(center);
        double b = ray.dir.dot(center);

        // The left side is R sin(t + theta0) with R = sqrt(a^2 + b^2), so it
        // never gets past R in either direction. If cos(radius) is outside
        // that range the ray misses.
        double R = Math.sqrt(a * a + b * b);
        if (Math.abs(cosRadius) > R)
            return null;

        double theta0 = Math.atan2(a, b);
        double alpha = Math.asin(cosRadius / R);

        // The two solutions in one trip round the great circle.
        double t1 = normalizeAngle(alpha - theta0);
        double t2 = normalizeAngle(Math.PI - alpha - theta0);

        // Nearest one that isn't at the ray's own origin.
        double t = Math.min(t1, t2);
        if (t < MathUtil.EPSILON) {
            t = Math.max(t1, t2);
            if (t < MathUtil.EPSILON)
                return null;
        }

        Vec4 hitPoint = ray.getPoint(t);
        Vec4 normal = computeNormal(hitPoint);

        return new Intersection(t, hitPoint, normal, this);
    }

    // Wrap an angle into [0, 2pi)
    private double normalizeAngle(double t) {
        while (t < 0)
            t += 2 * Math.PI;
        while (t >= 2 * Math.PI)
            t -= 2 * Math.PI;
        return t;
    }

    private Vec4 computeNormal(Vec4 p) {
        // Tangent at p pointing back towards the centre is c - p(c.p).
        // The outward normal is the opposite of that.
        double dot = center.dot(p);
        Vec4 n = p.scale(dot).minus(center);
        return n.normalize();
    }
}
