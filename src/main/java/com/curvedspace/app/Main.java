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
            frame.addKeyListener(input); // Add listener to frame
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.requestFocusInWindow();

            // Setup Initial Scene
            // Default to Hypercube as it's the "cool" one
            final Scene[] currentScene = { SceneFactory.createHypercubeScene() };

            // Cam at (0,0,0,1)
            Camera camera = new Camera(
                    new Vec4(0, 0, 0, 1),
                    new Vec4(0, 0, 1, 0), // Target
                    new Vec4(0, 1, 0, 0), // Up Hint
                    Math.toRadians(70));

            // RayTracer needs to be able to swap scenes or we create new one
            // Let's make RayTracer mutable or just recreate it?
            // Recreating is cheap (just holding refs).
            final RayTracer[] tracer = { new RayTracer(currentScene[0], camera, WIDTH, HEIGHT) };

            // Render Loop using Timer
            Timer timer = new Timer(16, e -> { // ~60 FPS target
                update(camera, input, image, currentScene, tracer, WIDTH, HEIGHT);
                tracer[0].render(image);
                panel.repaint();
            });
            timer.start();
        });
    }

    private static void update(Camera camera, InputHandler input, BufferedImage image, Scene[] sceneRef,
            RayTracer[] tracerRef, int w, int h) {
        double moveSpeed = 0.05;
        double turnSpeed = 0.03;

        // Scene Switching
        if (input.isPressed(KeyEvent.VK_1)) {
            if (sceneRef[0].getClass().getName().contains("Hypercube"))
                return; // Very hacky check, better to track ID
            sceneRef[0] = SceneFactory.createHypercubeScene();
            tracerRef[0] = new RayTracer(sceneRef[0], camera, w, h);
            System.out.println("Switched to Hypercube Scene");
        }
        if (input.isPressed(KeyEvent.VK_2)) {
            sceneRef[0] = SceneFactory.createEarthScene();
            tracerRef[0] = new RayTracer(sceneRef[0], camera, w, h);
            System.out.println("Switched to Earth Scene");
        }

        if (input.isPressed(KeyEvent.VK_W)) {
            camera.moveForward(moveSpeed);
        }
        if (input.isPressed(KeyEvent.VK_S)) {
            camera.moveForward(-moveSpeed);
        }

        if (input.isPressed(KeyEvent.VK_A)) {
            camera.yaw(turnSpeed);
        }
        if (input.isPressed(KeyEvent.VK_D)) {
            camera.yaw(-turnSpeed);
        }

        if (input.isPressed(KeyEvent.VK_UP)) {
            camera.pitch(turnSpeed);
        }
        if (input.isPressed(KeyEvent.VK_DOWN)) {
            camera.pitch(-turnSpeed);
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
