package com.curvedspace.tracer;

import com.curvedspace.geometry.Ray;
import com.curvedspace.geometry.S3Space;
import com.curvedspace.math.Vec4;

public class Camera {
    protected Vec4 pos;
    protected Vec4 forward;
    protected Vec4 up;
    protected Vec4 right;
    protected double fov; // Radians, vertical

    public Camera(Vec4 pos, Vec4 target, Vec4 upHint, double fov) {
        this.pos = pos.normalize();
        this.fov = fov;
        lookAt(target, upHint);
    }

    public void lookAt(Vec4 target, Vec4 upHint) {
        // Compute forward vector in tangent space of pos
        // Direction from pos to target
        // Tangent of geodesic at pos
        this.forward = S3Space.tangent(pos, target).normalize();

        // Project upHint to tangent space
        Vec4 upProj = S3Space.tangent(pos, upHint);

        // Orthogonalize up against forward
        // up = upProj - forward * (upProj . forward)
        this.up = S3Space.tangent(forward, upProj).normalize();

        // Right vector?
        // In 4D we need a 3D tangent space at 'pos'.
        // We have forward and up. We need a 3rd vector 'right'.
        // We can define 'right' using the 4D mechanics?
        // Or simply cross product if we treat the 4D space specially?
        // Wait, the tangent space is 3D (R^3).
        // We have 2 vectors: F, U. We need R orthogonal to both.
        // We also have 'pos' which is normal to the tangent space.
        // So R is orthogonal to pos, F, U.
        // We can use a generalized cross product in R^4?
        // Cross(A, B, C) -> D.
        // Right = Cross(pos, forward, up).

        this.right = cross4(pos, forward, up).normalize();
    }

    // Generalized cross product in R^4
    // Returns vector orthogonal to a, b, c
    private Vec4 cross4(Vec4 a, Vec4 b, Vec4 c) {
        // Determinant expansion simulation
        // | i j k l |
        // | x1 y1 z1 w1|
        // | x2 y2 z2 w2|
        // | x3 y3 z3 w3|

        double x = det3(a.y, a.z, a.w, b.y, b.z, b.w, c.y, c.z, c.w);
        double y = -det3(a.x, a.z, a.w, b.x, b.z, b.w, c.x, c.z, c.w);
        double z = det3(a.x, a.y, a.w, b.x, b.y, b.w, c.x, c.y, c.w);
        double w = -det3(a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);

        return new Vec4(x, y, z, w);
    }

    private double det3(double m11, double m12, double m13,
            double m21, double m22, double m23,
            double m31, double m32, double m33) {
        return m11 * (m22 * m33 - m23 * m32)
                - m12 * (m21 * m33 - m23 * m31)
                + m13 * (m21 * m32 - m22 * m31);
    }

    public Ray generateRay(double u, double v) {
        // u, v in [-1, 1] usually (ndc)
        // Adjust for aspect ratio elsewhere or pass in correct u/v scaling?
        // Assume u, v are scaled by tan(fov/2).

        // dir = normalize(forward + right * u + up * v)
        Vec4 dir = forward.plus(right.scale(u)).plus(up.scale(v)).normalize();

        return new Ray(pos, dir);
    }

    public Vec4 getPos() {
        return pos;
    }

    // Move along the forward vector
    public void moveForward(double dt) {
        // P(t) = P cos t + F sin t
        // F(t) = -P sin t + F cos t
        double c = Math.cos(dt);
        double s = Math.sin(dt);

        Vec4 pNew = pos.scale(c).plus(forward.scale(s));
        Vec4 fNew = pos.scale(-s).plus(forward.scale(c));

        this.pos = pNew.normalize();
        this.forward = fNew.normalize();

        // re-orthogonalize others just in case of drift
        this.up = S3Space.tangent(forward, S3Space.tangent(pos, up)).normalize();
        this.right = cross4(pos, forward, up).normalize();
    }

    // Rotate around Up vector (Yaw)
    public void yaw(double dt) {
        // Rotate Forward and Right
        double c = Math.cos(dt);
        double s = Math.sin(dt);

        Vec4 fNew = forward.scale(c).plus(right.scale(s));
        Vec4 rNew = forward.scale(-s).plus(right.scale(c));

        this.forward = fNew.normalize();
        this.right = rNew.normalize();

        // Orthogonalize up to ensure stability
        this.up = cross4(right, pos, forward).normalize(); // Order matters for coord system (R, P, F -> U?)
        // R = P x F x U.
        // U = R x P x F ?
        // Let's rely on orthogonalization against plane
        // Or cross4.
    }

    // Rotate around Right vector (Pitch)
    public void pitch(double dt) {
        // Rotate Forward and Up
        double c = Math.cos(dt);
        double s = Math.sin(dt);

        Vec4 fNew = forward.scale(c).plus(up.scale(s));
        Vec4 uNew = forward.scale(-s).plus(up.scale(c));

        this.forward = fNew.normalize();
        this.up = uNew.normalize();

        // Stabilize right
        this.right = cross4(pos, forward, up).normalize();
    }

    public double getFov() {
        return fov;
    }
}
