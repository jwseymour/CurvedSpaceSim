package render;

import manifold.Manifold;
import manifold.Vec3;
import manifold.euclidean.EuclideanSpace;
import manifold.hyperbolic.HyperboloidPoint;
import manifold.hyperbolic.HyperbolicSpace;
import manifold.spherical.SpherePoint;
import manifold.spherical.SphericalSpace;

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
    private final List<ManifoldSphere<SpherePoint>> sphericalSpheres = new ArrayList<>();
    private ManifoldPlane<SpherePoint> sphericalPlane = null;

    // Rendering parameters
    private int width = 800;
    private int height = 450;
    private double maxDistance = 50.0;
    private double stepSize = 0.05;
    private int maxSteps = 1000;

    // Lighting
    private Vec3 lightDir = new Vec3(0.5, 1, 0.5).normalize();
    private Vec3 ambientColor = new Vec3(0.1, 0.1, 0.15);
    private Vec3 skyColor = new Vec3(0.1, 0.1, 0.2);

    // Fog for hyperbolic space (objects fade toward boundary)
    private boolean fogEnabled = true;
    private double fogDensity = 0.05;
    private Vec3 fogColor = new Vec3(0.02, 0.02, 0.05);

    // The manifold we're rendering in
    private Object manifold;
    private ManifoldType manifoldType = ManifoldType.EUCLIDEAN;

    public enum ManifoldType {
        EUCLIDEAN,
        HYPERBOLIC,
        SPHERICAL
    }

    public RayTracer(Camera camera) {
        this.camera = camera;
        this.manifold = EuclideanSpace.INSTANCE;
    }

    public void setManifold(EuclideanSpace space) {
        this.manifold = space;
        this.manifoldType = ManifoldType.EUCLIDEAN;
    }

    public void setManifold(HyperbolicSpace space) {
        this.manifold = space;
        this.manifoldType = ManifoldType.HYPERBOLIC;
    }

    public void setManifold(SphericalSpace space) {
        this.manifold = space;
        this.manifoldType = ManifoldType.SPHERICAL;
    }

    /**
     * Set up the spherical scene with manifold-native objects.
     * These use geodesic distance for proper hit detection.
     */
    public void setupSphericalScene(SphericalSpace space) {
        sphericalSpheres.clear();

        // Create spheres in the manifold
        // Position them using the manifold's pointFromR3
        try {
            sphericalSpheres.add(new ManifoldSphere<>(space,
                    space.pointFromR3(new Vec3(-0.3, 0, -1)), 0.2, new Vec3(0.8, 0.2, 0.2)));
            sphericalSpheres.add(new ManifoldSphere<>(space,
                    space.pointFromR3(new Vec3(0.3, 0.1, -0.8)), 0.15, new Vec3(0.2, 0.6, 0.8)));
            sphericalSpheres.add(new ManifoldSphere<>(space,
                    space.pointFromR3(new Vec3(0, 0.2, -1.5)), 0.25, new Vec3(0.3, 0.8, 0.3)));
        } catch (Exception e) {
            // If positions are invalid, skip
        }

        // Create the floor plane (a geodesic plane at y = -0.2 in R³ coords)
        try {
            SpherePoint floorRef = space.pointFromR3(new Vec3(0, -0.2, 0));
            sphericalPlane = new ManifoldPlane<>(space, floorRef, Vec3.Y, 0.5,
                    new Vec3(0.9, 0.9, 0.9), new Vec3(0.2, 0.2, 0.2));
        } catch (Exception e) {
            sphericalPlane = null;
        }
    }

    public void addObject(SceneObject obj) {
        objects.add(obj);
    }

    public void clearObjects() {
        objects.clear();
        sphericalSpheres.clear();
        sphericalPlane = null;
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        camera.setAspectRatio((double) width / height);
    }

    public void setMaxDistance(double d) {
        this.maxDistance = d;
    }

    public void setStepSize(double s) {
        this.stepSize = s;
    }

    public void setFogEnabled(boolean enabled) {
        this.fogEnabled = enabled;
    }

    public void setFogDensity(double density) {
        this.fogDensity = density;
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
    private Vec3 traceRay(GeodesicRay ray) {
        switch (manifoldType) {
            case HYPERBOLIC:
                return traceRayHyperbolic(ray);
            case SPHERICAL:
                return traceRaySpherical(ray);
            case EUCLIDEAN:
            default:
                return traceRayEuclidean(ray);
        }
    }

    /**
     * Trace ray in Euclidean space (straight lines).
     */
    private Vec3 traceRayEuclidean(GeodesicRay ray) {
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
     * Trace ray in hyperbolic space using geodesic stepping.
     * We step along geodesics in the manifold, projecting to Poincaré ball for
     * visualization
     * and checking for intersections between consecutive projected positions.
     */
    private Vec3 traceRayHyperbolic(GeodesicRay ray) {
        HyperbolicSpace space = (HyperbolicSpace) manifold;

        // Convert camera position to hyperboloid
        Vec3 pos3D = ray.origin;
        Vec3 dir = ray.direction.normalize();

        // Check if position is valid for Poincaré ball (|pos| < 1)
        if (pos3D.normSq() >= 0.99) {
            pos3D = pos3D.normalize().scale(0.9);
        }

        HyperboloidPoint pos;
        try {
            pos = space.pointFromR3(pos3D);
        } catch (Exception e) {
            return skyColor;
        }

        double traveled = 0;
        Vec3 prevProjPos = space.projectToR3(pos);

        try {
            for (int step = 0; step < maxSteps && traveled < maxDistance; step++) {
                // Step along geodesic in manifold
                Vec3 stepVec = dir.scale(stepSize);
                HyperboloidPoint newPos = space.exp(pos, stepVec);

                // Project new position to Poincaré ball (R³)
                Vec3 projPos = space.projectToR3(newPos);

                // Check if we're approaching the boundary
                if (projPos.normSq() > 0.98) {
                    break;
                }

                // Check for floor crossing between prevProjPos and projPos
                // Floor is at y = floorY
                double floorY = -0.3;
                if (prevProjPos.y > floorY && projPos.y <= floorY) {
                    // Crossed the floor - interpolate hit point
                    double t = (floorY - prevProjPos.y) / (projPos.y - prevProjPos.y);
                    Vec3 hitPoint = prevProjPos.lerp(projPos, t);
                    Vec3 hitNormal = Vec3.Y;

                    // Checkerboard pattern
                    int cx = (int) Math.floor(hitPoint.x / 0.5);
                    int cz = (int) Math.floor(hitPoint.z / 0.5);
                    boolean even = ((cx + cz) % 2 == 0);
                    Vec3 floorColor = even ? new Vec3(0.9, 0.9, 0.9) : new Vec3(0.2, 0.2, 0.2);

                    return shadeSimple(floorColor, hitNormal, traveled + t * stepSize);
                }

                // Check for sphere intersections
                for (SceneObject obj : objects) {
                    if (obj instanceof Sphere3D) {
                        Sphere3D sphere = (Sphere3D) obj;
                        Vec3 toCenter = sphere.getCenter().sub(projPos);
                        double dist = toCenter.norm();
                        if (dist < sphere.getRadius()) {
                            // Inside sphere - we hit it
                            Vec3 hitNormal = projPos.sub(sphere.getCenter()).normalize();
                            return shadeSimple(sphere.getColor(projPos, hitNormal), hitNormal, traveled);
                        }
                    }
                }

                // Parallel transport the direction
                Vec3 newDir = space.parallelTransport(dir, pos, newPos);
                dir = newDir.normalize();

                prevProjPos = projPos;
                pos = newPos;
                traveled += stepSize;
            }
        } catch (Exception e) {
            return applyFog(skyColor, traveled);
        }

        return applyFog(skyColor, traveled);
    }

    /**
     * Simple shading for direct color computation.
     */
    private Vec3 shadeSimple(Vec3 color, Vec3 normal, double distance) {
        double diffuse = Math.max(0.2, normal.dot(lightDir));
        Vec3 lit = color.scale(diffuse + 0.2);
        return applyFog(lit, distance);
    }

    /**
     * Trace ray in spherical space using geodesic stepping.
     * Uses manifold-native objects with geodesic distance for proper wrapping.
     * Light rays travel along great circles and can hit the same object multiple
     * times!
     */
    private Vec3 traceRaySpherical(GeodesicRay ray) {
        SphericalSpace space = (SphericalSpace) manifold;

        Vec3 pos3D = ray.origin;
        Vec3 dir = ray.direction.normalize();

        SpherePoint pos;
        try {
            pos = space.pointFromR3(pos3D);
        } catch (Exception e) {
            return skyColor;
        }

        double traveled = 0;
        // In S³ with radius R, maximum interesting distance is 2πR (full circle)
        // After that we're back where we started
        double maxDist = Math.PI * space.getRadius() * 2;

        // Track previous position for plane crossing detection
        double prevPlaneDist = (sphericalPlane != null) ? sphericalPlane.signedDistance(pos) : 0;

        try {
            for (int step = 0; step < maxSteps && traveled < maxDist; step++) {
                // Check manifold-native spheres FIRST (these are the proper curved-space
                // spheres)
                for (ManifoldSphere<SpherePoint> sphere : sphericalSpheres) {
                    if (sphere.contains(pos)) {
                        Vec3 normal = sphere.getNormal(pos);
                        return shadeSimple(sphere.getColor(), normal, traveled);
                    }
                }

                // Check manifold-native plane crossing (from BOTH directions in S³)
                if (sphericalPlane != null) {
                    double currPlaneDist = sphericalPlane.signedDistance(pos);
                    // Crossing from above (positive to non-positive)
                    if (prevPlaneDist > 0 && currPlaneDist <= 0) {
                        Vec3 floorColor = sphericalPlane.getColor(pos);
                        return shadeSimple(floorColor, sphericalPlane.getNormal(), traveled);
                    }
                    // Crossing from below (negative to non-negative) - seeing underside of floor
                    if (prevPlaneDist < 0 && currPlaneDist >= 0) {
                        Vec3 floorColor = sphericalPlane.getColor(pos);
                        // Flip normal for underside
                        return shadeSimple(floorColor.scale(0.7), sphericalPlane.getNormal().negate(), traveled);
                    }
                    prevPlaneDist = currPlaneDist;
                }

                // Step along geodesic in manifold
                Vec3 stepVec = dir.scale(stepSize);
                SpherePoint newPos = space.exp(pos, stepVec);

                // Parallel transport direction along geodesic
                dir = space.parallelTransport(dir, pos, newPos).normalize();
                pos = newPos;
                traveled += stepSize;
            }
        } catch (Exception e) {
            return applyFog(skyColor, traveled);
        }

        return applyFog(skyColor, traveled);
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
        return applyFog(color, distance);
    }

    /**
     * Apply distance fog.
     */
    private Vec3 applyFog(Vec3 color, double distance) {
        if (!fogEnabled)
            return color;

        double fog = 1.0 - Math.exp(-fogDensity * distance);
        return color.lerp(fogColor, fog);
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
