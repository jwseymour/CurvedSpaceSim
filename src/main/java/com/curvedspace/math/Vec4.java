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

    @Override
    public String toString() {
        return String.format("Vec4(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}
