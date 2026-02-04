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

    // cross4 and det3 removed (using Vec4.cross now)

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
        // Vec4.cross(P, F, U) -> Right.

        Vec4 newRight = Vec4.cross(pos, forward, trueUp);
        if (newRight.length() < 1e-4) {
            // Forward is vertical. Keep existing right?
            newRight = S3Space.tangent(pos, right); // Start with old right
            newRight = S3Space.tangent(trueUp, newRight); // Make perp to TrueUp
        }
        this.right = newRight.normalize();

        this.up = trueUp;
        // Level Forward
        this.forward = Vec4.cross(pos, up, right).normalize();
    }

    @Override
    public void yaw(double angle) {
        // Yaw around WORLD UP.
        double c = Math.cos(angle);
        double s = Math.sin(angle);

        Vec4 k = up; // Axis

        // Rotate Forward
        // term2 = cross(pos, k, forward)
        Vec4 kxf = Vec4.cross(pos, k, forward);
        Vec4 newForward = forward.scale(c).plus(kxf.scale(s)).normalize();

        // Rotate Right
        Vec4 kxr = Vec4.cross(pos, k, right);
        Vec4 newRight = right.scale(c).plus(kxr.scale(s)).normalize();

        this.forward = newForward;
        this.right = newRight;
    }

    // cross4 and det3 removed (using Vec4.cross now)
}
