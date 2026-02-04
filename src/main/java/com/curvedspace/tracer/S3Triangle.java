package com.curvedspace.tracer;

import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;
import com.curvedspace.geometry.Ray;

public class S3Triangle implements S3Object {
    private final Vec4[] v = new Vec4[3];
    private final Vec4 normal; // Normal to the 3-plane containing A,B,C
    private final Vec4[] edgeNormals = new Vec4[3]; // Normals to edge planes (inside the 3-plane)
    private final Vec3 color;

    public S3Triangle(Vec4 v0, Vec4 v1, Vec4 v2, Vec3 color) {
        this.v[0] = v0;
        this.v[1] = v1;
        this.v[2] = v2;
        this.color = color;

        // 1. Calculate Plane Normal
        // This is the normal to the 3D subspace spanned by v0, v1, v2.
        // If v0,v1,v2 are close, this is stable.
        this.normal = Vec4.cross(v0, v1, v2).normalize();

        // 2. Calculate Edge Plane Normals
        // Edge 0-1: Plane passing through 0, 1, and Origin.
        // Wait, to distinguish "inside/outside" relative to v2?
        // We want a normal N_01 such that P.N_01 > 0 for P on same side as v2.
        // N_01 must be orthogonal to v0 and v1.
        // And it must lie IN the triangle's 3-plane (so orthogonal to this.normal).
        // So N_01 is orthogonal to v0, v1, normal.
        // N_01 = cross(v0, v1, normal).

        // Edge 0: v0 -> v1. Check v2.
        this.edgeNormals[0] = calcEdgeNormal(v[0], v[1], v[2]);
        // Edge 1: v1 -> v2. Check v0.
        this.edgeNormals[1] = calcEdgeNormal(v[1], v[2], v[0]);
        // Edge 2: v2 -> v0. Check v1.
        this.edgeNormals[2] = calcEdgeNormal(v[2], v[0], v[1]);
    }

    private Vec4 calcEdgeNormal(Vec4 a, Vec4 b, Vec4 check) {
        Vec4 n = Vec4.cross(a, b, normal).normalize();
        if (n.dot(check) < 0) {
            return n.scale(-1);
        }
        return n;
    }

    @Override
    public Intersection intersect(Ray ray) {
        // Ray: O cos t + D sin t
        // Plane: P . N = 0
        // (O.N) cos t + (D.N) sin t = 0

        double on = ray.origin.dot(normal);
        double dn = ray.dir.dot(normal);

        // solve: on + dn * tan(t) = 0 => tan(t) = -on/dn
        if (Math.abs(dn) < 1e-9)
            return null; // Parallel

        double t = Math.atan2(-on, dn);
        // atan2 returns value in -pi to pi.
        // We want smallest positive t.
        if (t < 0)
            t += Math.PI; // Intersects at t and t+pi (antipodal).
        // Check both? Usually we only see "front"? But S^3 is transparent.
        // Let's take the first hit.
        // Wait, Ray has max distance? Not really, usually PI.
        // If the triangle is "behind" us, t might be > pi / 2?

        // Let's refine t. atan2(y, x) -> tan = y/x.
        // atan2(-on, dn) gives angle where sin ~ -on, cos ~ dn.
        // Check: (O cos t + D sin t).N = on * dn - dn * on = 0? No.
        // Check equation: on * cos + dn * sin = 0.
        // sin/cos = -on/dn.

        // Intersection point
        Vec4 p = ray.getPoint(t);

        // Check barycentric / edge sides
        for (int i = 0; i < 3; i++) {
            if (p.dot(edgeNormals[i]) < -1e-9) {
                return null; // Outside
            }
        }

        return new Intersection(t, p, this.normal, this);
    }

    @Override
    public Vec3 getColor(Vec4 point) {
        return color;
    }
}
