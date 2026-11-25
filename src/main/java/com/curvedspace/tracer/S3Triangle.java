package com.curvedspace.tracer;

import com.curvedspace.math.MathUtil;
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

        // Normal to the 3-plane through the origin and the three vertices.
        this.normal = Vec4.cross(v0, v1, v2).normalize();

        // Each edge gets a normal that lies in that 3-plane, is orthogonal to both
        // ends of the edge, and points towards the third vertex. A point is inside
        // the triangle when it is on the positive side of all three.
        this.edgeNormals[0] = calcEdgeNormal(v[0], v[1], v[2]);
        this.edgeNormals[1] = calcEdgeNormal(v[1], v[2], v[0]);
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
        // The ray is O cos t + D sin t, so it meets the triangle's 3-plane where
        // (O.N) cos t + (D.N) sin t = 0, i.e. tan t = -(O.N) / (D.N).
        double on = ray.origin.dot(normal);
        double dn = ray.dir.dot(normal);

        // Both zero means the whole geodesic lies in the plane.
        if (Math.abs(on) < 1e-9 && Math.abs(dn) < 1e-9)
            return null;

        double t = Math.atan2(-on, dn);
        if (t < MathUtil.EPSILON)
            t += Math.PI;

        // A great circle crosses the plane twice, at antipodal points half a turn
        // apart. Only one of them can be inside the triangle, and it may be the
        // far one, so try both.
        for (int k = 0; k < 2; k++, t += Math.PI) {
            Vec4 p = ray.getPoint(t);
            if (inside(p))
                return new Intersection(t, p, this.normal, this);
        }
        return null;
    }

    private boolean inside(Vec4 p) {
        for (Vec4 edgeNormal : edgeNormals) {
            if (p.dot(edgeNormal) < -1e-9)
                return false;
        }
        return true;
    }

    @Override
    public Vec3 getColor(Vec4 point) {
        return color;
    }
}
