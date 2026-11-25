package com.curvedspace.tracer;

import com.curvedspace.math.Vec4;

public class GroundCamera extends Camera {
    // The ground is a sphere centred on (0,-1,0,0). Height above it is asin(y),
    // so walking at a fixed height means keeping the y coordinate fixed.
    private final double heightY;

    public GroundCamera(double heightAngle, Vec4 pos, Vec4 target, Vec4 upHint, double fov) {
        super(pos, target, upHint, fov);
        this.heightY = Math.sin(heightAngle);
    }

    @Override
    public void moveForward(double dt) {
        // 1. Calculate Movement Direction (Horizontal)
        // We move along the geodesic tangent to the ground.
        // Current Ground Normal
        Vec4 currentTrueUp = new Vec4(0, 1, 0, 0).minus(pos.scale(heightY)).normalize();

        // Level Forward = Vector in the tangent plane of 'pos' that is also orthogonal
        // to 'currentTrueUp'
        // We use 'right' to define the orientation azimuth.
        // F_level = cross(pos, trueUp, right).
        Vec4 moveDir = Vec4.cross(pos, currentTrueUp, right).normalize();

        // 2. Transport Camera Frame along moveDir
        double c = Math.cos(dt);
        double s = Math.sin(dt);

        Vec4 pNew = pos.scale(c).plus(moveDir.scale(s)).normalize();

        // Transport basis vectors
        // Rotate vector V in plane (pos, moveDir) by angle dt.
        // V_new = V_perp + (V . moveDir) * (-pos*s + moveDir*c)
        // Since V is tangent to pos (V.pos = 0), V_perp = V - (V.moveDir)*moveDir

        this.pos = pNew;
        this.forward = transport(forward, pos, moveDir, c, s);
        this.up = transport(up, pos, moveDir, c, s);
        this.right = transport(right, pos, moveDir, c, s);

        // 3. Snap 'pos' back to valid height
        double currentXZW_sq = pos.x * pos.x + pos.z * pos.z + pos.w * pos.w;
        double targetXZW_sq = 1.0 - heightY * heightY;

        if (currentXZW_sq > 1e-9) {
            double scale = Math.sqrt(targetXZW_sq / currentXZW_sq);
            this.pos = new Vec4(pos.x * scale, heightY, pos.z * scale, pos.w * scale);
        }

        // 4. Re-align with Gravity (Remove Roll, Preserve Pitch)
        // New True Up at new position
        Vec4 trueUp = new Vec4(0, 1, 0, 0).minus(pos.scale(heightY)).normalize();

        // Recalculate Right to be horizontal
        Vec4 newRight = Vec4.cross(pos, forward, trueUp);

        if (newRight.length() > 1e-4) {
            this.right = newRight.normalize();
            // forward was transported before pos was snapped, so it is no
            // longer exactly tangent at pos. Rebuild up from right and
            // forward, then forward from up and right. For small steps this
            // keeps the pitch.
            this.up = Vec4.cross(pos, right, forward).normalize();
            this.forward = Vec4.cross(pos, up, right).normalize();

        } else {
            // Singularity (Looking straight up/down along trueUp)
            // Keep existing Right (transported), just fix Up and Forward
            this.up = Vec4.cross(pos, right, forward).normalize();
            this.forward = Vec4.cross(pos, up, right).normalize();
        }
    }

    private Vec4 transport(Vec4 v, Vec4 pOld, Vec4 dir, double c, double s) {
        // Parallel transport of v along the geodesic from pOld in direction
        // dir. The part of v along dir rotates with the geodesic and the
        // rest is unchanged.
        double val = v.dot(dir);
        Vec4 v_along = dir.scale(val);
        Vec4 v_perp = v.minus(v_along);

        Vec4 v_rot = pOld.scale(-s).plus(dir.scale(c)).scale(val);

        return v_perp.plus(v_rot).normalize();
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
}
