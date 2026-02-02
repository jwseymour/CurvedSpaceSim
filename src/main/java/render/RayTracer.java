package render;

import manifold.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Non-Euclidean ray tracer.
 * 
 * Traces geodesics through curved space by:
 * 1. Casting rays from the camera
 * 2. Stepping along geodesics using the manifold's exp map
 * 3. Testing for intersections with scene objects
 * 4. Shading based on lighting and material
 */
public class RayTracer {

    private final List<SceneObject> objects = new ArrayList<>();
    private final Camera camera;

    // Manifold-native scene objects for spherical space

    // Rendering parameters
    private int width = 800;
    private int height = 450;
    private double maxDistance = 50.0;

    // Lighting
    private Vec3 lightDir = new Vec3(0.5, 1, 0.5).normalize();
    private Vec3 skyColor = new Vec3(0.1, 0.1, 0.2);

    public RayTracer(Camera camera) {
        this.camera = camera;
        // Euclidean by default
    }

    /**
     * Set up the spherical scene with manifold-native objects.
     * These use geodesic distance for proper hit detection.
     */

    public void addObject(SceneObject obj) {
        objects.add(obj);
    }

    public void clearObjects() {
        objects.clear();
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        camera.setAspectRatio((double) width / height);
    }

    public void setMaxDistance(double d) {
        this.maxDistance = d;
    }

    public void setLightDirection(Vec3 dir) {
        this.lightDir = dir.normalize();
    }

    /**
     * Render the scene to an RGB array.
     * 
     * @return Array of RGB values [height][width][3], each in [0, 255]
     */
    public int[][][] render() {
        int[][][] image = new int[height][width][3];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                double u = (i + 0.5) / width;
                double v = (j + 0.5) / height;

                GeodesicRay ray = camera.getRay(u, v);
                Vec3 color = traceRay(ray);

                // Convert to 8-bit color
                image[j][i][0] = clamp((int) (color.x * 255), 0, 255);
                image[j][i][1] = clamp((int) (color.y * 255), 0, 255);
                image[j][i][2] = clamp((int) (color.z * 255), 0, 255);
            }
        }

        return image;
    }

    /**
     * Render a single pixel (for interactive preview).
     */
    public Vec3 renderPixel(int i, int j) {
        double u = (i + 0.5) / width;
        double v = (j + 0.5) / height;
        GeodesicRay ray = camera.getRay(u, v);
        return traceRay(ray);
    }

    /**
     * Trace a ray through the scene.
     */
    /**
     * Trace a ray through the scene.
     */
    private Vec3 traceRay(GeodesicRay ray) {
        // Simple flat-space ray tracing - just check intersections directly
        Hit closestHit = null;

        for (SceneObject obj : objects) {
            Hit hit = obj.intersect(ray, 0.001, maxDistance);
            if (hit != null && (closestHit == null || hit.t < closestHit.t)) {
                closestHit = hit;
            }
        }

        if (closestHit != null) {
            return shade(closestHit, ray, closestHit.t);
        }

        return skyColor;
    }

    /**
     * Compute shading for a hit point.
     */
    private Vec3 shade(Hit hit, GeodesicRay ray, double distance) {
        Vec3 baseColor = hit.object.getColor(hit.point, hit.normal);

        // Lambertian diffuse lighting
        double diffuse = Math.max(0, hit.normal.dot(lightDir));

        // Simple specular highlight
        Vec3 viewDir = ray.direction.negate();
        Vec3 reflectDir = lightDir.negate().reflect(hit.normal);
        double specular = Math.pow(Math.max(0, viewDir.dot(reflectDir)), 32) * 0.3;

        // Combine lighting
        Vec3 color = baseColor.scale(diffuse + 0.3) // Diffuse + ambient
                .add(new Vec3(specular, specular, specular));

        // Apply fog
        return color; // Fog removed
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Camera getCamera() {
        return camera;
    }
}
