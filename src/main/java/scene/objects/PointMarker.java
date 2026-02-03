package scene.objects;

import manifold.Vec3;

/**
 * A small marker to visualize a point in the scene.
 * Implemented as a small geodesic circle.
 */
public class PointMarker extends GeodesicCircle {

    public PointMarker(Vec3 center, double radius, Vec3 color) {
        super(center, radius, color);
    }
}
