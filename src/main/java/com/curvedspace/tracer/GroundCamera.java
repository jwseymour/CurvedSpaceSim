package com.curvedspace.tracer;

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
            // Recalculate Up to be orthogonal to Forward and Right (Preserves Pitch)
            // But forward is dirty!
            // We want 'Up' to be in the plane defined by 'Old Forward' and 'True Up'.
            // Actually, we want to Keep 'Pitch'.
            // Pitch is angle between Forward and Horizon.
            // If we keep 'Up' orthogonal to 'Right' and 'Forward', we are good.
            // Wait, we need to FIX the basis.

            // Current State:
            // 'pos' is snapped (NEW).
            // 'right' is horizontal (NEW, perp to pos and TrueUp).
            // 'forward' is OLD (transported, not perp to pos).

            // We want to construct a new basis (F, U, R) at 'pos'.
            // Constraint 1: R is horizontal (already done).
            // Constraint 2: Maintain Pitch.
            // Let's deduce 'Forward' and 'Up' from the old Forward's projection?

            // Simpler approach that works for small steps:
            // 1. We computed 'right' from 'forward' and 'trueUp'.
            // 2. Now compute 'up' from 'right' and 'forward'.
            this.up = Vec4.cross(pos, right, forward).normalize();

            // 3. Now force 'forward' to be orthogonal to 'up' and 'right'.
            this.forward = Vec4.cross(pos, up, right).normalize();

        } else {
            // Singularity (Looking straight up/down along trueUp)
            // Keep existing Right (transported), just fix Up and Forward
            this.up = Vec4.cross(pos, right, forward).normalize();
            this.forward = Vec4.cross(pos, up, right).normalize();
        }
    }

    private Vec4 transport(Vec4 v, Vec4 pOld, Vec4 dir, double c, double s) {
        // v is vector at pOld.
        // We want v at pNew.
        // Component along dir:
        double val = v.dot(dir);
        Vec4 v_along = dir.scale(val);
        Vec4 v_perp = v.minus(v_along);

        // Rotated component
        // dir maps to (-pOld * s + dir * c)
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

    // cross4 and det3 removed (using Vec4.cross now)
}
