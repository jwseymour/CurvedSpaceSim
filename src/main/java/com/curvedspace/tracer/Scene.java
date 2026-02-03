package com.curvedspace.tracer;

import com.curvedspace.geometry.Ray;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private final List<S3Object> objects = new ArrayList<>();

    public void add(S3Object obj) {
        objects.add(obj);
    }

    public Intersection intersect(Ray ray) {
        Intersection closest = null;
        for (S3Object obj : objects) {
            Intersection hit = obj.intersect(ray);
            if (hit != null) {
                if (closest == null || hit.distance < closest.distance) {
                    closest = hit;
                }
            }
        }
        return closest;
    }
}
