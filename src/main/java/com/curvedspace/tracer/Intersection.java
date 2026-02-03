package com.curvedspace.tracer;

import com.curvedspace.math.Vec4;

public class Intersection {
    public final double distance;
    public final Vec4 point;
    public final Vec4 normal;
    public final S3Object object;

    public Intersection(double distance, Vec4 point, Vec4 normal, S3Object object) {
        this.distance = distance;
        this.point = point;
        this.normal = normal;
        this.object = object;
    }
}
