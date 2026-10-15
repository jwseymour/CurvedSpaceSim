package com.curvedspace.tracer;

import com.curvedspace.geometry.Ray;
import com.curvedspace.math.Vec3;

public interface S3Object {
    /**
     * Test for intersection.
     * 
     * @param ray The ray to test against.
     * @return The closest Intersection or null if no intersection.
     */
    Intersection intersect(Ray ray);

    /**
     * Get material color at a specific point.
     * 
     * @param point The point on the object surface.
     * @return RGB color as Vec3.
     */
    Vec3 getColor(com.curvedspace.math.Vec4 point);
}
