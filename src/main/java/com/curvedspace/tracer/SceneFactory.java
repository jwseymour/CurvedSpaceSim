package com.curvedspace.tracer;

import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;

public class SceneFactory {

    public static Scene createHypercubeScene() {
        Scene scene = new Scene();
        // 16 vertices of a tesseract
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    for (int w = -1; w <= 1; w += 2) {
                        Vec4 pos = new Vec4(x, y, z, w).normalize();
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
        double groundDist = 0.5;
        double groundRadius = (Math.PI / 2.0) - groundDist;

        Vec4 groundCenter = new Vec4(0, -1, 0, 0);
        scene.add(new CheckerboardSphere(
                groundCenter,
                groundRadius,
                new Vec3(0.1, 0.6, 0.1),
                new Vec3(0.1, 0.4, 0.1),
                10.0));

        int numTrees = 8;
        for (int i = 0; i < numTrees; i++) {
            double angle = i * 2 * Math.PI / numTrees;
            double d = 1.0;
            double x = Math.sin(angle) * d;
            double z = Math.cos(angle) * d;
            scene.add(new Sphere(new Vec4(x, 0, z, 1).normalize(), 0.1, new Vec3(0.5, 0.25, 0.0)));
            scene.add(new Sphere(new Vec4(x, 0.1, z, 1).normalize(), 0.15, new Vec3(0.0, 0.8, 0.0)));
        }
        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.3, new Vec3(1.0, 1.0, 0.0)));
        return scene;
    }

    public static Scene createGreatSphereScene() {
        Scene scene = new Scene();
        double groundRadius = Math.PI / 2.0;

        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.2, 0.2, 0.8),
                new Vec3(0.1, 0.1, 0.4),
                16.0));

        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.2, new Vec3(1, 1, 0.8)));
        scene.add(new Sphere(new Vec4(0, 0, 1, 0), 0.3, new Vec3(0.8, 0.8, 0.8)));

        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0;
            double x = Math.sin(angle);
            double z = Math.cos(angle);
            Vec4 p = new Vec4(x, 0, z, 0).normalize();
            Vec4 pos = p.plus(new Vec4(0, 0.2, 0, 0)).normalize();
            scene.add(new Sphere(pos, 0.1, new Vec3(1, 0, 0)));
        }
        return scene;
    }

    public static Scene createCityScene() {
        Scene scene = new Scene();
        double groundRadius = Math.PI / 2.0;

        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.1, 0.4, 0.05),
                new Vec3(0.05, 0.3, 0.02),
                24.0));

        int numBlocks = 12;
        double streetWidth = 0.3;

        for (int i = 0; i < numBlocks; i++) {
            double angle = i * 2 * Math.PI / numBlocks;
            double t = angle;

            Vec4 posLeft = new Vec4(streetWidth, 0.05, Math.cos(t), Math.sin(t)).normalize();
            scene.add(new Sphere(posLeft, 0.12, new Vec3(0.8, 0.7, 0.5)));
            Vec4 roofPos = posLeft.plus(new Vec4(0, 0.1, 0, 0)).normalize();
            scene.add(new Sphere(roofPos, 0.1, new Vec3(0.7, 0.1, 0.1)));

            Vec4 posRight = new Vec4(-streetWidth, 0.05, Math.cos(t), Math.sin(t)).normalize();
            scene.add(new Sphere(posRight, 0.05, new Vec3(0.4, 0.2, 0.1)));
            Vec4 leafPos = posRight.plus(new Vec4(0, 0.15, 0, 0)).normalize();
            scene.add(new Sphere(leafPos, 0.12, new Vec3(0.0, 0.6, 0.1)));

            Vec4 roadPos = new Vec4(0, 0, Math.cos(t), Math.sin(t)).normalize();
            scene.add(new Sphere(roadPos, 0.02, new Vec3(0.3, 0.3, 0.3)));
        }

        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.2, new Vec3(1.0, 1.0, 0.8)));
        return scene;
    }

    public static Scene createPyramidScene() {
        Scene scene = new Scene();
        double groundRadius = Math.PI / 2.0;
        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.5, 0.5, 0.5),
                new Vec3(0.3, 0.3, 0.3),
                10.0));

        double size = 0.3;
        Vec4 v0 = new Vec4(0, 0.5, 1, 0).normalize();
        Vec4 v1 = new Vec4(0, 0.1, 1 - size, -size).normalize();
        Vec4 v2 = new Vec4(size, 0.1, 1 - size, size).normalize();
        Vec4 v3 = new Vec4(-size, 0.1, 1 - size, size).normalize();

        scene.add(new S3Triangle(v0, v1, v2, new Vec3(1, 0, 0)));
        scene.add(new S3Triangle(v0, v2, v3, new Vec3(0, 1, 0)));
        scene.add(new S3Triangle(v0, v3, v1, new Vec3(0, 0, 1)));
        scene.add(new S3Triangle(v1, v3, v2, new Vec3(1, 1, 0)));

        return scene;
    }

    // Scene 6: Triangle House
    public static Scene createTriangleHouseScene() {
        Scene scene = new Scene();

        // Ground
        double groundRadius = Math.PI / 2.0;
        scene.add(new CheckerboardSphere(
                new Vec4(0, -1, 0, 0),
                groundRadius,
                new Vec3(0.2, 0.6, 0.2), // Grass
                new Vec3(0.1, 0.4, 0.1),
                16.0));

        // Offset House to the side (relative to "Road")
        // Road is along x=0. House at x=0.4?
        double houseX = 0.4;

        double W = 0.2;
        double D = 0.2;
        double H = 0.25;
        double RH = 0.15;

        // Center shift not needed as we apply directly

        // Helper adjusted to include shift

        Vec4 b00 = makeHouseVert(-W + houseX, 0, -D);
        Vec4 b10 = makeHouseVert(W + houseX, 0, -D);
        Vec4 b11 = makeHouseVert(W + houseX, 0, D);
        Vec4 b01 = makeHouseVert(-W + houseX, 0, D);

        Vec4 c00 = makeHouseVert(-W + houseX, H, -D);
        Vec4 c10 = makeHouseVert(W + houseX, H, -D);
        Vec4 c11 = makeHouseVert(W + houseX, H, D);
        Vec4 c01 = makeHouseVert(-W + houseX, H, D);

        Vec4 peak = makeHouseVert(0 + houseX, H + RH, 0);

        Vec3 wallColor = new Vec3(0.9, 0.8, 0.7);
        Vec3 roofColor = new Vec3(0.8, 0.2, 0.1);
        Vec3 doorColor = new Vec3(0.4, 0.2, 0.1);

        addQuad(scene, b00, b10, c10, c00, wallColor);
        addQuad(scene, b10, b11, c11, c10, wallColor);
        addQuad(scene, b11, b01, c01, c11, wallColor);
        addQuad(scene, b01, b00, c00, c01, wallColor);

        scene.add(new S3Triangle(c00, c10, peak, roofColor));
        scene.add(new S3Triangle(c10, c11, peak, roofColor));
        scene.add(new S3Triangle(c11, c01, peak, roofColor));
        scene.add(new S3Triangle(c01, c00, peak, roofColor));

        // Door
        double dW = 0.08;
        double dH = 0.15;
        Vec4 d00 = makeHouseVert(-dW + houseX, 0, -D - 0.01);
        Vec4 d10 = makeHouseVert(dW + houseX, 0, -D - 0.01);
        Vec4 d11 = makeHouseVert(dW + houseX, dH, -D - 0.01);
        Vec4 d01 = makeHouseVert(-dW + houseX, dH, -D - 0.01);
        addQuad(scene, d00, d10, d11, d01, doorColor);

        // --- ROAD / FENCE ---
        // Fence posts along the line x=0.
        // This Great Circle loop goes all the way around.
        int numPosts = 40;
        for (int i = 0; i < numPosts; i++) {
            double angle = i * 2 * Math.PI / numPosts;
            // Circle in Y=0 plane (Equator) but restricted to X=0?
            // X=0, Y=0 is a circle in Z-W plane. Vertices: (0, 0, cos t, sin t).
            // But we want it on the ground. Ground is surface of sphere centered at
            // (0,-1,0,0).
            // Equator (y=0) is ON the ground.
            // So circle x=0, y=0 is ON the ground.

            // Posts along (0, 0, cos t, sin t).
            double z = Math.cos(angle);
            double w = Math.sin(angle);

            Vec4 postPos = new Vec4(0, 0, z, w).normalize();
            // Post: small white cylinder/sphere
            scene.add(new Sphere(postPos, 0.03, new Vec3(0.9, 0.9, 0.9)));
            // Top knot
            Vec4 postTop = postPos.plus(new Vec4(0, 0.06, 0, 0)).normalize();
            scene.add(new Sphere(postTop, 0.04, new Vec3(1.0, 1.0, 1.0)));
        }

        // --- TREES ---
        // Spread out trees on the OTHER side of the road (x < 0).
        int numTrees = 12;
        for (int i = 0; i < numTrees; i++) {
            double angle = i * 2 * Math.PI / numTrees + 0.5; // Offset phase
            double z = Math.cos(angle);
            double w = Math.sin(angle);

            // Vary distance from road
            double dist = -0.3 - (Math.abs(Math.sin(angle * 3)) * 0.3); // Random-ish distance

            // We want y approx 0.
            // x = dist.
            // z = z_circle.
            // w = w_circle.

            // To be on surface y=0, x^2+z^2+w^2 = 1.
            // if x is fixed, z,w are on circle of radius sqrt(1-x^2).
            double r = Math.sqrt(Math.max(0, 1 - dist * dist));
            Vec4 basePos = new Vec4(dist, 0, z * r, w * r).normalize();

            // Trunk
            scene.add(new Sphere(basePos, 0.07, new Vec3(0.4, 0.25, 0.1)));
            // Leaves
            Vec4 lPos = basePos.plus(new Vec4(0, 0.25, 0, 0)).normalize();
            scene.add(new Sphere(lPos, 0.2, new Vec3(0.05, 0.5, 0.1)));
        }

        // Sun
        scene.add(new Sphere(new Vec4(0, 1, 0, 0), 0.2, new Vec3(1.0, 1.0, 0.8)));

        return scene;
    }

    private static Vec4 makeHouseVert(double x, double y, double w) {
        return new Vec4(x, y, 1.0, w).normalize();
    }

    private static void addQuad(Scene scene, Vec4 v00, Vec4 v10, Vec4 v11, Vec4 v01, Vec3 color) {
        scene.add(new S3Triangle(v00, v10, v11, color));
        scene.add(new S3Triangle(v00, v11, v01, color));
    }
}
