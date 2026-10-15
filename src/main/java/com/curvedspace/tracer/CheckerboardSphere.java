package com.curvedspace.tracer;

import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;

public class CheckerboardSphere extends Sphere {
    private final Vec3 color2;
    private final double scale;

    public CheckerboardSphere(Vec4 center, double radius, Vec3 color1, Vec3 color2, double scale) {
        super(center, radius, color1);
        this.color2 = color2;
        this.scale = scale;
    }

    @Override
    public Vec3 getColor(Vec4 point) {
        // Project point to local coords or use angles?
        // Simple 4D checkerboard: simple based on signs of coordinates?
        // Or based on angles?
        // Let's use 4D position checkerboard.
        double s = scale;
        boolean cx = (Math.floor(point.x * s) % 2 == 0);
        boolean cy = (Math.floor(point.y * s) % 2 == 0);
        boolean cz = (Math.floor(point.z * s) % 2 == 0);
        boolean cw = (Math.floor(point.w * s) % 2 == 0);

        int count = (cx ? 1 : 0) + (cy ? 1 : 0) + (cz ? 1 : 0) + (cw ? 1 : 0);
        if (count % 2 == 0) {
            return super.getColor(point);
        } else {
            return color2;
        }
    }
}
