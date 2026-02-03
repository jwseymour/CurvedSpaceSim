package com.curvedspace.geometry;

import com.curvedspace.math.Vec4;

public class Ray {
    public final Vec4 origin; // Point on S^3
    public final Vec4 dir; // Unit tangent vector at origin

    public Ray(Vec4 origin, Vec4 dir) {
        this.origin = origin;
        this.dir = dir;
    }

    // Get point at distance t along the geodesic
    public Vec4 getPoint(double t) {
        // formula: P cos t + V sin t
        return origin.scale(Math.cos(t)).plus(dir.scale(Math.sin(t)));
    }

    // Get tangent vector at distance t along the geodesic
    public Vec4 getDir(double t) {
        // derivative: -P sin t + V cos t
        return origin.scale(-Math.sin(t)).plus(dir.scale(Math.cos(t)));
    }
}
