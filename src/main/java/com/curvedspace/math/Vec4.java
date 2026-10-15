package com.curvedspace.math;

public class Vec4 {
    public final double x, y, z, w;

    public Vec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4 plus(Vec4 other) {
        return new Vec4(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vec4 minus(Vec4 other) {
        return new Vec4(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    public Vec4 scale(double s) {
        return new Vec4(x * s, y * s, z * s, w * s);
    }

    public double dot(Vec4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vec4 normalize() {
        double len = length();
        if (len < MathUtil.EPSILON) {
            return new Vec4(0, 0, 0, 0); // Or throw exception? Zero vector is safe.
        }
        return scale(1.0 / len);
    }

    /**
     * Calculate 4D cross product (orthogonal to 3 vectors).
     */
    public static Vec4 cross(Vec4 a, Vec4 b, Vec4 c) {
        double x = det3(a.y, a.z, a.w, b.y, b.z, b.w, c.y, c.z, c.w);
        double y = -det3(a.x, a.z, a.w, b.x, b.z, b.w, c.x, c.z, c.w);
        double z = det3(a.x, a.y, a.w, b.x, b.y, b.w, c.x, c.y, c.w);
        double w = -det3(a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
        return new Vec4(x, y, z, w);
    }

    private static double det3(double m11, double m12, double m13, double m21, double m22, double m23, double m31,
            double m32, double m33) {
        return m11 * (m22 * m33 - m23 * m32) - m12 * (m21 * m33 - m23 * m31) + m13 * (m21 * m32 - m22 * m31);
    }

    @Override
    public String toString() {
        return String.format("Vec4(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}
