package com.curvedspace.app;

import com.curvedspace.tracer.Camera;
import com.curvedspace.tracer.RayTracer;
import com.curvedspace.tracer.Scene;
import com.curvedspace.tracer.SceneFactory;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Interactive viewer: ray traces the selected scene on S^3 and lets the user
 * fly through it with the keyboard and mouse.
 */
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
            final Scene[] currentScene = { null };
            final Camera[] camera = { null };
            final RayTracer[] tracer = { null };
            loadScene(0, currentScene, camera, tracer, WIDTH, HEIGHT);

            // Render Loop using Timer
            Timer timer = new Timer(16, e -> { // ~60 FPS target
                update(camera, input, image, currentScene, tracer, WIDTH, HEIGHT);
                tracer[0].render(image);
                panel.repaint();
            });
            timer.start();
        });
    }

    /** Display names of the scenes bound to keys 1-6. */
    static final String[] SCENE_NAMES = { "Hypercube", "Earth", "Infinite Earth", "City", "Pyramid",
            "Triangle House" };

    /** Replaces the scene, camera and tracer with those for scene {@code index}. */
    private static void loadScene(int index, Scene[] sceneRef, Camera[] cameraRef, RayTracer[] tracerRef,
            int w, int h) {
        sceneRef[0] = SceneFactory.create(index);
        cameraRef[0] = SceneFactory.createCamera(index);
        tracerRef[0] = new RayTracer(sceneRef[0], cameraRef[0], w, h);
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

        // Scene Switching (keys 1-6)
        for (int i = 0; i < SCENE_NAMES.length; i++) {
            if (input.isPressed(KeyEvent.VK_1 + i)) {
                loadScene(i, sceneRef, cameraRef, tracerRef, w, h);
                System.out.println("Switched to " + SCENE_NAMES[i] + " scene");
            }
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
