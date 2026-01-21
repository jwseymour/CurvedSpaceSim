package render;

import manifold.Vec3;

/**
 * Base interface for objects that can be hit by a ray.
 */
public interface SceneObject {

    /**
     * Test for intersection with a ray.
     * 
     * @param ray  The ray to test
     * @param tMin Minimum distance (to avoid self-intersection)
     * @param tMax Maximum distance to consider
     * @return Hit record if intersection found, null otherwise
     */
    Hit intersect(GeodesicRay ray, double tMin, double tMax);

    /**
     * Get the color at a surface point.
     * 
     * @param point  The point on the surface
     * @param normal The surface normal
     * @return RGB color as Vec3 (components in [0, 1])
     */
    Vec3 getColor(Vec3 point, Vec3 normal);
}
