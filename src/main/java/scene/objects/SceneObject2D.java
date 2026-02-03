package scene.objects;

import geometry.Space2D;
import rays.Ray2D;
import manifold.Vec3;

/**
 * A 2D object in the scene (Euclidean or Spherical).
 */
public interface SceneObject2D {
    /**
     * Intersect rule: returns closest positive t along ray.
     * Returns Double.POSITIVE_INFINITY if no intersection.
     */
    double intersect(Ray2D ray, Space2D space);

    /**
     * Get the object's color.
     */
    Vec3 getColor();

    /**
     * Get the position/center of the object (used for 2D game rendering).
     */
    Vec3 getPosition();
}
