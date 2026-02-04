package com.curvedspace.tracer;

import com.curvedspace.geometry.S3Space;
import com.curvedspace.math.Vec4;

public class GroundCamera extends Camera {
    // "Great Sphere" ground is defined by y=0 (relative to south pole -1)
    // Actually, ground center is (0,-1,0,0). Equator is y=0.
    // If we want height H, we want constant y coordinate.
    // P = (x, y, z, w). H = asin(y).
    // To stay at height H, we must keep y constant.
    // Movement must be restricted to the x,z,w hyperplane?

    private final double heightY;

    public GroundCamera(double heightAngle, Vec4 pos, Vec4 target, Vec4 upHint, double fov) {
        super(pos, target, upHint, fov);
        // We enforce y component = sin(heightAngle)
        this.heightY = Math.sin(heightAngle);
    }

    // Copy cross4 here since it's private in Camera. Ideally move to MathUtil.
    private Vec4 cross4(Vec4 a, Vec4 b, Vec4 c) {
        double x = det3(a.y, a.z, a.w, b.y, b.z, b.w, c.y, c.z, c.w);
        double y = -det3(a.x, a.z, a.w, b.x, b.z, b.w, c.x, c.z, c.w);
        double z = det3(a.x, a.y, a.w, b.x, b.y, b.w, c.x, c.y, c.w);
        double w = -det3(a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
        return new Vec4(x, y, z, w);
    }

    private double det3(double m11, double m12, double m13, double m21, double m22, double m23, double m31, double m32,
            double m33) {
        return m11 * (m22 * m33 - m23 * m32) - m12 * (m21 * m33 - m23 * m31) + m13 * (m21 * m32 - m22 * m31);
    }

    @Override
    public void moveForward(double dt) {
        // 1. Move normally along geodesic
        super.moveForward(dt);

        // 2. Snap 'pos' back to the latitude defined by heightY
        // We want pos.y = heightY.
        // We scale x,z,w so that x^2+z^2+w^2 = 1 - heightY^2.

        // Check current XZW magnitude
        double currentXZW_sq = pos.x * pos.x + pos.z * pos.z + pos.w * pos.w;
        double targetXZW_sq = 1.0 - heightY * heightY;

        if (currentXZW_sq > 1e-9) {
            double scale = Math.sqrt(targetXZW_sq / currentXZW_sq);
            this.pos = new Vec4(pos.x * scale, heightY, pos.z * scale, pos.w * scale);
        }

        // 3. World Gravity Alignment
        // At position P, the "World Up" (away from South Pole (0,-1,0,0) and towards
        // North Pole (0,1,0,0))
        // is along the meridian.
        // True Up U = Gradient of y coordinate at surface.
        // U = (0,1,0,0) - P * y
        // We defined pos.y = heightY.
        // U = (0,1,0,0) - P * heightY. Normalize.

        Vec4 trueUp = new Vec4(0, 1, 0, 0).minus(pos.scale(heightY)).normalize();

        // 4. Force 'up' to match True Up to prevent rolling/flipping
        // We want to preserve look direction but fix 'up'.
        // However, 'forward' as transported might be diving.
        // Calculate new Right vector first (Horizontal).
        // Right = Forward x TrueUp.
        // If forward is parallel to TrueUp (looking straight up/down), this fails.
        // Assuming we aren't at singularity:

        Vec4 newRight = cross4(pos, forward, trueUp);
        if (newRight.length() < 1e-4) {
            // Forward is vertical. Keep existing right?
            // Project existing right onto horizontal plane?
            newRight = S3Space.tangent(pos, right); // Start with old right
            newRight = S3Space.tangent(trueUp, newRight); // Make perp to TrueUp
        }
        this.right = newRight.normalize();

        // Now reconstruct Forward and Up
        this.up = trueUp;
        // Forward = Up x Right?
        // Wait, standard basis: Right = Forward x Up.
        // So Forward = Up x Right is WRONG direction?
        // Right = F x U.
        // F = U x R? Let's check R3: X = Y x Z? No, Z = X x Y.
        // X = (1,0,0), Y = (0,1,0), Z = (0,0,1).
        // Y x Z = (1,0,0) = X.
        // So Forward = Up x Right.

        // However, we want to preserve Pitch.
        // If we just set Forward = Up x Right, we force Forward to be horizontal (Pitch
        // 0).
        // We need to rotate this horizontal forward by the original pitch angle?
        // But original pitch is lost in the transport.
        // Let's assume for "Walking" we WANT to level the view?
        // User complains "start looking down". Just leveling it is a good fix for
        // walking.
        // But if they WANT to look up, we shouldn't drift down.

        // Let's just LEVEL the forward vector here.
        // Pitch control is manual anyway.
        this.forward = cross4(pos, up, right).normalize();
    }

    @Override
    public void yaw(double angle) {
        // Yaw around WORLD UP, not local up.
        // We are in tangent space of Pos.
        // Rotation axis is TrueUp = `up`.
        // We rotate `forward` and `right` around `up`.

        // Rodrigues rotation of vector V around axis K:
        // V_rot = V cos(a) + (K x V) sin(a) + K(K.V)(1-cos a)
        // Here K = up. K.V = 0 (since basis is orthonormal).
        // So V_rot = V cos(a) + (K x V) sin(a).
        // K x V? In 4D tangent space of P, K x V is cross4(P, K, V).

        double c = Math.cos(angle);
        double s = Math.sin(angle);

        Vec4 k = up; // Axis

        // Rotate Forward
        // term2 = cross4(pos, k, forward)
        // Wait, cross4(P, F, U) = R.
        // cross4(P, U, F) = -R.
        Vec4 kxf = cross4(pos, k, forward);
        Vec4 newForward = forward.scale(c).plus(kxf.scale(s)).normalize();

        // Rotate Right
        // axis is U. Vector is R.
        // cross4(P, U, R) = F?
        // cross4(P, F, U) = R => cross4(P, R, F) = U => cross4(P, U, R) = F.
        Vec4 kxr = cross4(pos, k, right);
        Vec4 newRight = right.scale(c).plus(kxr.scale(s)).normalize();

        this.forward = newForward;
        this.right = newRight;
        // Up remains unchanged (axis of rotation)
    }
}
