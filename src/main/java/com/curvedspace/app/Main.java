package com.curvedspace.app;

import com.curvedspace.math.Vec4;
import com.curvedspace.tracer.Camera;
import com.curvedspace.tracer.RayTracer;
import com.curvedspace.tracer.Scene;
import com.curvedspace.tracer.SceneFactory;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main {
    private static final int WIDTH = 800; // Increased res slightly
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Curved Space Simulation (S^3)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            DisplayPanel panel = new DisplayPanel(image);
            InputHandler input = new InputHandler();

            frame.add(panel);
            frame.addKeyListener(input);
            frame.addMouseListener(input); // Add mouse listener
            frame.addMouseMotionListener(input); // Add motion listener
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.requestFocusInWindow();

            // Setup Initial Scene
            // Default to Hypercube as it's the "cool" one
            final Scene[] currentScene = { SceneFactory.createHypercubeScene() };

            // Camera container
            final Camera[] camera = { new Camera(
                    new Vec4(0, 0, 0, 1),
                    new Vec4(0, 0, 1, 0), // Target
                    new Vec4(0, 1, 0, 0), // Up Hint
                    Math.toRadians(70)) };

            final RayTracer[] tracer = { new RayTracer(currentScene[0], camera[0], WIDTH, HEIGHT) };

            // Render Loop using Timer
            Timer timer = new Timer(16, e -> { // ~60 FPS target
                update(camera, input, image, currentScene, tracer, WIDTH, HEIGHT);
                tracer[0].render(image);
                panel.repaint();
            });
            timer.start();
        });
    }

    private static void update(Camera[] cameraRef, InputHandler input, BufferedImage image, Scene[] sceneRef,
            RayTracer[] tracerRef, int w, int h) {
        double moveSpeed = 0.05;
        double turnSpeed = 0.03;

        Camera cam = cameraRef[0];

        // Mouse Look
        int dx = input.getDx();
        int dy = input.getDy();
        double mouseSense = 0.005; // Radians per pixel

        if (dx != 0) {
            cam.yaw(-dx * mouseSense);
        }
        if (dy != 0) {
            cam.pitch(-dy * mouseSense);
        }

        // Scene Switching
        if (input.isPressed(KeyEvent.VK_1)) {
            if (!sceneRef[0].getClass().getName().contains("Hypercube")) {
                sceneRef[0] = SceneFactory.createHypercubeScene();
                // Reset camera
                cameraRef[0] = new Camera(new Vec4(0, 0, 0, 1), new Vec4(0, 0, 1, 0), new Vec4(0, 1, 0, 0),
                        Math.toRadians(70));
                tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
                System.out.println("Switched to Hypercube Scene");
            }
        }
        if (input.isPressed(KeyEvent.VK_2)) {
            // Earth Scene
            sceneRef[0] = SceneFactory.createEarthScene();
            cameraRef[0] = new Camera(new Vec4(0, 0, 0, 1), new Vec4(0, 0, 1, 0), new Vec4(0, 1, 0, 0),
                    Math.toRadians(70));
            tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
            System.out.println("Switched to Earth Scene");
        }
        if (input.isPressed(KeyEvent.VK_3)) {
            // Infinite Earth Scene
            sceneRef[0] = SceneFactory.createGreatSphereScene();

            // Use GroundCamera!
            // Height = equator latitude?
            // Ground is centered at south pole. Equator is dist pi/2.
            // We want to be slightly "above" the equator (y > 0)?
            // If ground radius is pi/2, then surface is at equator (y=0).
            // Let's float at y = 0.1 (sin(angle)=0.1).
            double height = Math.asin(0.1);
            // Pos must satisfy y=0.1. e.g. (0.995, 0.1, 0, 0)
            Vec4 startPos = new Vec4(Math.cos(height), 0.1, 0, 0).normalize();

            cameraRef[0] = new com.curvedspace.tracer.GroundCamera(
                    height,
                    startPos,
                    new Vec4(0, 0.1, 1, 0), // Look along z, keeping y approx const
                    new Vec4(0, 1, 0, 0), // Up is Y axis
                    Math.toRadians(70));

            tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
            System.out.println("Switched to Infinite Earth Scene");
        }
        if (input.isPressed(KeyEvent.VK_4)) {
            // "Real Life" City Scene
            sceneRef[0] = SceneFactory.createCityScene();

            // GroundCamera also needed here!
            double height = 0.1;
            // Pos must satisfy y=sin(0.1) ~ 0.1? No, we used y=heightY in GroundCamera.
            // If we use asin(0.1), then y=0.1.
            // Let's use same settings as Inf Earth.
            double hAngle = Math.asin(0.1);
            Vec4 startPos = new Vec4(Math.cos(hAngle), 0.1, 0, 0).normalize();

            cameraRef[0] = new com.curvedspace.tracer.GroundCamera(
                    hAngle,
                    startPos,
                    new Vec4(0, 0.1, 1, 0),
                    new Vec4(0, 1, 0, 0),
                    Math.toRadians(70));

            tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
            System.out.println("Switched to City Scene");
        }
        if (input.isPressed(KeyEvent.VK_5)) {
            // Pyramid Scene
            sceneRef[0] = SceneFactory.createPyramidScene();

            // Use GroundCamera
            double hAngle = Math.asin(0.1);
            Vec4 startPos = new Vec4(Math.cos(hAngle), 0.1, 0, 0).normalize();

            cameraRef[0] = new com.curvedspace.tracer.GroundCamera(
                    hAngle,
                    startPos,
                    new Vec4(0, 0.1, 1, 0),
                    new Vec4(0, 1, 0, 0),
                    Math.toRadians(70));

            tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
            System.out.println("Switched to Pyramid Scene");
        }
        if (input.isPressed(KeyEvent.VK_6)) {
            // Triangle House
            sceneRef[0] = SceneFactory.createTriangleHouseScene();

            double hAngle = Math.asin(0.1);
            Vec4 startPos = new Vec4(Math.cos(hAngle), 0.1, 0, 0).normalize();

            cameraRef[0] = new com.curvedspace.tracer.GroundCamera(
                    hAngle,
                    startPos,
                    new Vec4(0, 0.1, 1, 0),
                    new Vec4(0, 1, 0, 0),
                    Math.toRadians(70));

            tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
            System.out.println("Switched to Triangle House Scene");
        }

        if (input.isPressed(KeyEvent.VK_W)) {
            cam.moveForward(moveSpeed);
        }
        if (input.isPressed(KeyEvent.VK_S)) {
            cam.moveForward(-moveSpeed);
        }

        if (input.isPressed(KeyEvent.VK_A)) {
            cam.yaw(turnSpeed);
        }
        if (input.isPressed(KeyEvent.VK_D)) {
            cam.yaw(-turnSpeed);
        }

        if (input.isPressed(KeyEvent.VK_UP)) {
            cam.pitch(turnSpeed);
        }
        if (input.isPressed(KeyEvent.VK_DOWN)) {
            cam.pitch(-turnSpeed);
        }

        if (input.isPressed(KeyEvent.VK_P)) {
            try {
                javax.imageio.ImageIO.write(image, "png", new java.io.File("screenshot_s3.png"));
                System.out.println("Screenshot saved.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
