package com.curvedspace.tracer;

import com.curvedspace.geometry.Ray;
import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RayTracer {
    private final Scene scene;
    private final Camera camera;
    private final int width;
    private final int height;

    public RayTracer(Scene scene, Camera camera, int width, int height) {
        this.scene = scene;
        this.camera = camera;
        this.width = width;
        this.height = height;
    }

    public void render(BufferedImage image) {
        // One task per scanline
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        double aspect = (double) width / height;
        // The camera takes u, v already scaled by tan(fov/2), with u also
        // scaled by the aspect ratio.
        double fovY = camera.getFov();
        double scaleY = Math.tan(fovY / 2.0);
        double scaleX = scaleY * aspect;

        for (int y = 0; y < height; y++) {
            final int finalY = y;
            executor.submit(() -> {
                for (int x = 0; x < width; x++) {
                    double u = ((x + 0.5) / width * 2 - 1) * scaleX;
                    double v = ((1.0 - (finalY + 0.5) / height) * 2 - 1) * scaleY; // flip Y

                    Ray ray = camera.generateRay(u, v);
                    Vec3 color = trace(ray);

                    int rgb = toRGB(color);
                    image.setRGB(x, finalY, rgb);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Vec3 trace(Ray ray) {
        Intersection hit = scene.intersect(ray);
        if (hit != null) {
            // Basic lighting
            Vec3 materialColor = hit.object.getColor(hit.point);

            // Headlight shading: light is at camera, so light vector is -rayDir
            Vec4 rayDirAtHit = ray.getDir(hit.distance); // Tangent at hit point
            Vec4 lightDir = rayDirAtHit.scale(-1.0);

            // Lambert
            double diffuse = Math.max(0.0, hit.normal.dot(lightDir));
            // Add some ambient
            double ambient = 0.2;
            double intensity = ambient + (1.0 - ambient) * diffuse;

            return materialColor.scale(intensity);
        }
        return new Vec3(0.05, 0.05, 0.1); // Background color (Deep Space/Dark Blue)
    }

    private int toRGB(Vec3 c) {
        int r = (int) (Math.min(1.0, c.x) * 255);
        int g = (int) (Math.min(1.0, c.y) * 255);
        int b = (int) (Math.min(1.0, c.z) * 255);
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
}
