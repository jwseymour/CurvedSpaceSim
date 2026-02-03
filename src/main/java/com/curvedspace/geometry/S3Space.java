package com.curvedspace.geometry;

import com.curvedspace.math.MathUtil;
import com.curvedspace.math.Vec4;

public class S3Space {

    // Distance between two points on S^3 is the angle between them (acos of dot
    // product)
    public static double distance(Vec4 p1, Vec4 p2) {
        double dot = p1.dot(p2);
        // clamp to [-1, 1] to avoid NaN from acos
        dot = MathUtil.clamp(dot, -1.0, 1.0);
        return Math.acos(dot);
    }

    // Projects a point in R^4 onto S^3 (normalization)
    public static Vec4 project(Vec4 v) {
        return v.normalize();
    }

    // Gram-Schmidt to make v orthogonal to p (project v onto tangent space of p)
    public static Vec4 tangent(Vec4 p, Vec4 v) {
        // v_tangent = v - (v . p) * p
        // Assumes p is normalized
        double dot = v.dot(p);
        return v.minus(p.scale(dot));
    }
}
