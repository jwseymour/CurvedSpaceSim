package com.curvedspace.math;

public class MathUtil {
    public static final double EPSILON = 1e-9;

    public static boolean approxEquals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
}
