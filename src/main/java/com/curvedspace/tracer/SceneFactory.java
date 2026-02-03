package com.curvedspace.tracer;

import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;

public class SceneFactory {

    public static Scene createHypercubeScene() {
        Scene scene = new Scene();
        // 16 vertices of a tesseract (hypercube)
        // Coords: (+-1, +-1, +-1, +-1) normalized

        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    for (int w = -1; w <= 1; w += 2) {
                        Vec4 pos = new Vec4(x, y, z, w).normalize();

                        // Color based on position to orient
                        // Map -1..1 to 0..1
                        float r = (x + 1) * 0.5f;
                        float g = (y + 1) * 0.5f;
                        float b = (z + 1) * 0.5f;

                        scene.add(new Sphere(pos, 0.15, new Vec3(r, g, b)));
                    }
                }
            }
        }
        return scene;
    }

    public static Scene createEarthScene() {
        Scene scene = new Scene();

        // 1. The Ground
        // Center it "below" the origin (0,0,0,1).
        // "Down" is usually -Y in 3D, let's use -Y axis in 4D basis relative to cam?
        // Camera starts at (0,0,0,1), Up is (0,1,0,0).
        // So "Down" is (0, -1, 0, 0).
        // Distance from (0,0,0,1) to (0,-1,0,0) is pi/2 ~ 1.57.
        // We want ground to be say 0.5 units below feet.
        // So radius = pi/2 - 0.5 ~ 1.07.
        double groundDist = 0.5;
        double groundRadius = (Math.PI / 2.0) - groundDist;

        // Use Checkered Ground
        Vec4 groundCenter = new Vec4(0, -1, 0, 0);
        scene.add(new CheckerboardSphere(
                groundCenter,
                groundRadius,
                new Vec3(0.1, 0.6, 0.1), // Base Green
                new Vec3(0.1, 0.4, 0.1), // Darker Green
                10.0 // Grid density
        ));

        // 2. Objects on the ground
        // We need to place them on the surface of the ground sphere.
        // Ground surface points are at distance groundRadius from (0,-1,0,0).
        // The camera is at (0,0,0,1). distance to (0,-1,0,0) is pi/2.

        // Let's create a ring of "Trees" around the player
        int numTrees = 8;
        for (int i = 0; i < numTrees; i++) {
            double angle = i * 2 * Math.PI / numTrees;
            // Circle in XZ plane around the camera?
            // Positions should be:
            // x = sin(theta) * dist
            // z = cos(theta) * dist
            // y = ?
            // Need to satisfy dist((x,y,z,w), (0,-1,0,0)) = groundRadius + treeRadius
            // Complicated.

            // Simpler: Just place spheres at fixed positions relative to Camera.
            double d = 1.0; // Distance from camera
            double x = Math.sin(angle) * d;
            double z = Math.cos(angle) * d;

            scene.add(new Sphere(new Vec4(x, 0, z, 1).normalize(), 0.1, new Vec3(0.5, 0.25, 0.0))); // Brown Trunk
            scene.add(new Sphere(new Vec4(x, 0.1, z, 1).normalize(), 0.15, new Vec3(0.0, 0.8, 0.0))); // Leaves
        }

        // A "Sun" high up?
        // (0, 1, 0, 0) is the "North Pole" relative to the ground.
        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.3, new Vec3(1.0, 1.0, 0.0)));

        return scene;
    }
}
