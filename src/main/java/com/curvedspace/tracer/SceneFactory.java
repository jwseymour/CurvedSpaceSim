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

    // Scene 3: "Infinite Earth"
    // Ground is a Great Sphere (Radius pi/2). Bisects the universe.
    // Camera must stay at fixed height, otherwise you walk INTO the ground
    // immediately.
    public static Scene createGreatSphereScene() {
        Scene scene = new Scene();

        // Ground is a Great Sphere centered at South Pole (0, -1, 0, 0).
        // Radius = pi/2.
        // It passes through the Equator (y=0).
        // Anything with y < 0 is "underground".
        // Anything with y > 0 is "above ground".
        double groundRadius = Math.PI / 2.0;

        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.2, 0.2, 0.8), // Blue-ish basic
                new Vec3(0.1, 0.1, 0.4), // Darker Blue
                16.0));

        // Add "Sun" at North Pole (0, 1, 0, 0) (Zenith)
        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.2, new Vec3(1, 1, 0.8)));

        // Add "Moon" at Forward Horizon?
        // Forward is (0, 0, 1, 0).
        // This lies ON the ground great sphere. So half rising/half setting.
        scene.add(new Sphere(new Vec4(0, 0, 1, 0), 0.3, new Vec3(0.8, 0.8, 0.8)));

        // Add pillars at various distances
        // Place them at fixed height relative to ground?
        // Ideally we generate them using math to place them ON the ground.
        // But for now, just place them slightly above equator.
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0;
            double x = Math.sin(angle);
            double z = Math.cos(angle);
            // Position on equator (y=0)
            Vec4 p = new Vec4(x, 0, z, 0).normalize();
            // Shift slightly up (positive y)
            Vec4 pos = p.plus(new Vec4(0, 0.2, 0, 0)).normalize();

            scene.add(new Sphere(pos, 0.1, new Vec3(1, 0, 0)));
        }

        return scene;
    }

    // Scene 4: "Real Life" / City
    // Green ground, houses, roads, trees.
    public static Scene createCityScene() {
        Scene scene = new Scene();

        // 1. Ground: Green Great Sphere
        double groundRadius = Math.PI / 2.0;

        // Define colors
        Vec3 green = new Vec3(0.1, 0.4, 0.05); // Grass
        Vec3 darkGreen = new Vec3(0.05, 0.3, 0.02);

        // Add Ground
        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                green,
                darkGreen,
                24.0 // High density grass
        ));

        // 2. Objects
        // Place random houses and trees along the equator (y=0).
        // Since we are moving in curved space, "roads" are great circles.

        // Create a "Street" along the Z-axis (x=0).
        // Objects placed at x = +/- constant.

        int numBlocks = 12;
        double streetWidth = 0.3; // Distance from centerline

        for (int i = 0; i < numBlocks; i++) {
            double angle = i * 2 * Math.PI / numBlocks;
            double z = Math.cos(angle);
            double w = Math.sin(angle); // Use W for depth along the street?
            // Actually, we are walking on (x, 0, z, w).

            // Let's place houses along a Great Circle in the X-Z plane?
            // Or X-W plane.

            // Let's make the "Road" the circle where x=0, y=0.
            // Vertices: (0, 0, cos t, sin t).
            // Houses at x = streetWidth.

            double t = angle;

            // Left side house
            Vec4 posLeft = new Vec4(streetWidth, 0.05, Math.cos(t), Math.sin(t)).normalize();
            // House Body (Beige cube-ish sphere)
            scene.add(new Sphere(posLeft, 0.12, new Vec3(0.8, 0.7, 0.5)));
            // Roof (Red sphere on top)
            Vec4 roofPos = posLeft.plus(new Vec4(0, 0.1, 0, 0)).normalize();
            scene.add(new Sphere(roofPos, 0.1, new Vec3(0.7, 0.1, 0.1)));

            // Right side tree
            Vec4 posRight = new Vec4(-streetWidth, 0.05, Math.cos(t), Math.sin(t)).normalize();
            // Trunk
            scene.add(new Sphere(posRight, 0.05, new Vec3(0.4, 0.2, 0.1)));
            // Leaves
            Vec4 leafPos = posRight.plus(new Vec4(0, 0.15, 0, 0)).normalize();
            scene.add(new Sphere(leafPos, 0.12, new Vec3(0.0, 0.6, 0.1)));

            // Add "Road" markers (Grey flat spheres embedded in ground?)
            // Just small grey stones along center
            Vec4 roadPos = new Vec4(0, 0, Math.cos(t), Math.sin(t)).normalize(); // y=0
            scene.add(new Sphere(roadPos, 0.02, new Vec3(0.3, 0.3, 0.3)));
        }

        // Sun at zenith
        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.2, new Vec3(1.0, 1.0, 0.8)));

        return scene;
    }

    // Scene 5: Single Pyramid (Tetrahedron)
    public static Scene createPyramidScene() {
        Scene scene = new Scene();

        // Add plain ground or infinite? Let's use Infinite Ground for context
        double groundRadius = Math.PI / 2.0;
        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.5, 0.5, 0.5),
                new Vec3(0.3, 0.3, 0.3),
                10.0));

        // Define Pyramid Vertices.
        // It's a tetrahedron. 4 vertices.
        // Let's float it in front of the camera (0, 0, 1, 0).
        // Center C = (0, 0.2, 1, 0).normalize().

        // Vertices relative to center?
        // Let's just hardcode 4 points around (0, 0, 1, 0).

        double size = 0.3;
        // Apex
        Vec4 v0 = new Vec4(0, 0.5, 1, 0).normalize();
        // Base triangle
        Vec4 v1 = new Vec4(0, 0.1, 1 - size, -size).normalize();
        Vec4 v2 = new Vec4(size, 0.1, 1 - size, size).normalize();
        Vec4 v3 = new Vec4(-size, 0.1, 1 - size, size).normalize();
        // Wait, regular tetrahedron is harder. Let's just use these.

        // 4 Faces (Triangles)
        // 1. v0, v1, v2
        scene.add(new S3Triangle(v0, v1, v2, new Vec3(1, 0, 0))); // Red
        // 2. v0, v2, v3
        scene.add(new S3Triangle(v0, v2, v3, new Vec3(0, 1, 0))); // Green
        // 3. v0, v3, v1
        scene.add(new S3Triangle(v0, v3, v1, new Vec3(0, 0, 1))); // Blue
        // 4. Base v1, v3, v2 (Orientation matter?)
        scene.add(new S3Triangle(v1, v3, v2, new Vec3(1, 1, 0))); // Yellow

        return scene;
    }
}
