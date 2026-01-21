import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import manifold.Vec3;
import manifold.euclidean.EuclideanSpace;
import manifold.hyperbolic.HyperboloidPoint;
import manifold.hyperbolic.HyperbolicSpace;
import manifold.spherical.SpherePoint;
import manifold.spherical.SphericalSpace;
import physics.ParticleSystem;
import render.*;

/**
 * Main application for the Non-Euclidean Manifold Renderer.
 * 
 * Controls:
 * - WASD: Move camera
 * - Mouse drag: Look around
 * - Scroll: Adjust FOV
 * - Space: Add particle at camera position
 * - R: Reset particles
 */
public class ManifoldMain extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 450;

    private Camera camera;
    private RayTracer rayTracer;

    // Geometry selection - larger radii = gentler curvature
    private EuclideanSpace euclidean = EuclideanSpace.INSTANCE;
    private HyperbolicSpace hyperbolic = new HyperbolicSpace(5.0); // Larger = gentler hyperbolic curvature
    private SphericalSpace spherical = new SphericalSpace(3.0); // R=3 means circumference = 2πR ≈ 19 units
    private String currentGeometry = "Euclidean";

    // Height lock feature
    private boolean heightLocked = false;
    private double lockedHeight = 0.2;

    // Particle systems (one per geometry)
    private ParticleSystem<Vec3> euclideanParticles;
    private ParticleSystem<HyperboloidPoint> hyperbolicParticles;
    private ParticleSystem<SpherePoint> sphericalParticles;

    // Rendering
    private WritableImage image;
    private boolean needsRender = true;
    private int renderQuality = 4; // Render every Nth pixel for preview

    // Input state
    private double lastMouseX, lastMouseY;
    private boolean dragging = false;
    private boolean wDown, aDown, sDown, dDown, qDown, eDown;
    private double moveSpeed = 0.1;

    @Override
    public void start(Stage stage) {
        // Initialize camera
        camera = new Camera();
        camera.setPosition(new Vec3(0, 0.2, 0.5));
        camera.setFov(70);
        camera.setAspectRatio((double) WIDTH / HEIGHT);

        // Initialize ray tracer
        rayTracer = new RayTracer(camera);
        rayTracer.setResolution(WIDTH, HEIGHT);
        rayTracer.setMaxDistance(20);
        rayTracer.setStepSize(0.02);
        rayTracer.setFogEnabled(true);
        rayTracer.setFogDensity(0.03);

        // Initialize particle systems
        euclideanParticles = new ParticleSystem<>(euclidean);
        hyperbolicParticles = new ParticleSystem<>(hyperbolic);
        sphericalParticles = new ParticleSystem<>(spherical);

        // Setup scene
        setupScene();

        // Create UI
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);

        image = new WritableImage(WIDTH, HEIGHT);

        // Controls panel
        VBox controls = new VBox(10);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #333;");

        // Geometry selector
        Label geoLabel = new Label("Geometry:");
        geoLabel.setTextFill(Color.WHITE);
        ComboBox<String> geoSelector = new ComboBox<>();
        geoSelector.getItems().addAll("Euclidean", "Hyperbolic", "Spherical");
        geoSelector.setValue("Euclidean");
        geoSelector.setOnAction(e -> {
            currentGeometry = geoSelector.getValue();
            resetCameraForGeometry();
            setupScene();
            needsRender = true;
        });

        // Quality slider
        Label qualLabel = new Label("Preview Quality:");
        qualLabel.setTextFill(Color.WHITE);
        Slider qualSlider = new Slider(1, 8, 4);
        qualSlider.setShowTickLabels(true);
        qualSlider.setMajorTickUnit(1);
        qualSlider.setBlockIncrement(1);
        qualSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            renderQuality = newVal.intValue();
            needsRender = true;
        });

        // Height lock checkbox
        javafx.scene.control.CheckBox heightLockCheck = new javafx.scene.control.CheckBox("Lock Height");
        heightLockCheck.setTextFill(Color.WHITE);
        heightLockCheck.setSelected(heightLocked);
        heightLockCheck.setOnAction(e -> {
            heightLocked = heightLockCheck.isSelected();
            if (heightLocked) {
                lockedHeight = camera.getPosition().y;
            }
        });

        // Instructions
        Label instructions = new Label(
                "WASD: Move | QE: Up/Down\n" +
                        "Mouse Drag: Look\n" +
                        "Space: Add Particle\n" +
                        "R: Reset Particles\n" +
                        "H: Toggle Height Lock");
        instructions.setTextFill(Color.LIGHTGRAY);

        controls.getChildren().addAll(
                geoLabel, geoSelector,
                qualLabel, qualSlider,
                heightLockCheck,
                instructions);

        root.setRight(controls);

        // Animation loop
        GraphicsContext gc = canvas.getGraphicsContext2D();
        new AnimationTimer() {
            private long lastFrame = 0;

            @Override
            public void handle(long now) {
                // Update at ~60fps
                if (now - lastFrame >= 16_000_000) {
                    update(1.0 / 60.0);
                    lastFrame = now;
                }

                // Render if needed
                if (needsRender) {
                    renderFrame(gc);
                    needsRender = false;
                }
            }
        }.start();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Non-Euclidean Manifold Renderer");

        // Input handlers - must be set AFTER scene is attached to stage
        setupInput(canvas, scene);

        stage.show();

        // Initial render
        needsRender = true;
    }

    private void setupScene() {
        rayTracer.clearObjects();

        // Add checkerboard floor
        rayTracer.addObject(CheckerboardPlane.horizontal(-0.5, 0.5));

        // Add some spheres
        rayTracer.addObject(new Sphere3D(new Vec3(-0.5, 0, -2), 0.3, new Vec3(0.8, 0.2, 0.2)));
        rayTracer.addObject(new Sphere3D(new Vec3(0.5, 0.1, -1.5), 0.2, new Vec3(0.2, 0.6, 0.8)));
        rayTracer.addObject(new Sphere3D(new Vec3(0, 0.2, -3), 0.4, new Vec3(0.3, 0.8, 0.3)));

        // Set manifold
        switch (currentGeometry) {
            case "Hyperbolic":
                rayTracer.setManifold(hyperbolic);
                // Add particle spheres
                for (SceneObject obj : hyperbolicParticles.getSceneObjects()) {
                    rayTracer.addObject(obj);
                }
                break;
            case "Spherical":
                rayTracer.setManifold(spherical);
                rayTracer.setupSphericalScene(spherical); // Use manifold-native objects for proper wrapping
                for (SceneObject obj : sphericalParticles.getSceneObjects()) {
                    rayTracer.addObject(obj);
                }
                break;
            case "Euclidean":
            default:
                rayTracer.setManifold(euclidean);
                for (SceneObject obj : euclideanParticles.getSceneObjects()) {
                    rayTracer.addObject(obj);
                }
                break;
        }
    }

    /**
     * Reset camera position to a good starting point for the current geometry.
     */
    private void resetCameraForGeometry() {
        switch (currentGeometry) {
            case "Hyperbolic":
                // Start near the center of the Poincaré ball for good visibility
                camera.setPosition(new Vec3(0, 0.1, 0.2));
                lockedHeight = 0.1;
                break;
            case "Spherical":
                // Start at a reasonable position
                camera.setPosition(new Vec3(0, 0.2, 0.5));
                lockedHeight = 0.2;
                break;
            case "Euclidean":
            default:
                camera.setPosition(new Vec3(0, 0.2, 0.5));
                lockedHeight = 0.2;
                break;
        }
        // Look toward negative Z (forward into the scene)
        camera.lookAt(camera.getPosition().add(new Vec3(0, 0, -1)), Vec3.Y);
    }

    private void setupInput(Canvas canvas, Scene scene) {
        canvas.setFocusTraversable(true);

        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            dragging = true;
        });

        canvas.setOnMouseReleased(e -> dragging = false);

        canvas.setOnMouseDragged(e -> {
            if (dragging) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                camera.rotate(dx * 0.005, dy * 0.005);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                needsRender = true;
            }
        });

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W:
                    wDown = true;
                    break;
                case A:
                    aDown = true;
                    break;
                case S:
                    sDown = true;
                    break;
                case D:
                    dDown = true;
                    break;
                case Q:
                    qDown = true;
                    break;
                case E:
                    eDown = true;
                    break;
                case SPACE:
                    addParticle();
                    break;
                case R:
                    resetParticles();
                    break;
                case H:
                    heightLocked = !heightLocked;
                    if (heightLocked) {
                        lockedHeight = camera.getPosition().y;
                    }
                    needsRender = true;
                    break;
                default:
                    break;
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W:
                    wDown = false;
                    break;
                case A:
                    aDown = false;
                    break;
                case S:
                    sDown = false;
                    break;
                case D:
                    dDown = false;
                    break;
                case Q:
                    qDown = false;
                    break;
                case E:
                    eDown = false;
                    break;
                default:
                    break;
            }
        });

        canvas.setOnScroll(e -> {
            double fov = Math.max(20, Math.min(120,
                    rayTracer.getCamera().getForward().norm() - e.getDeltaY() * 0.1));
            // FOV adjustment could be implemented here
            needsRender = true;
        });
    }

    private void update(double dt) {
        // Camera movement
        double forward = (wDown ? 1 : 0) - (sDown ? 1 : 0);
        double right = (dDown ? 1 : 0) - (aDown ? 1 : 0);
        double up = (eDown ? 1 : 0) - (qDown ? 1 : 0);

        // If height is locked, ignore vertical movement
        if (heightLocked) {
            up = 0;
        }

        if (forward != 0 || right != 0 || up != 0) {
            camera.move(forward * moveSpeed, right * moveSpeed, up * moveSpeed);

            // Apply height lock
            if (heightLocked) {
                Vec3 pos = camera.getPosition();
                camera.setPosition(new Vec3(pos.x, lockedHeight, pos.z));
            }

            // In hyperbolic space, clamp position inside Poincaré ball
            // With R=5, the Poincaré ball effective range is larger
            if (currentGeometry.equals("Hyperbolic")) {
                Vec3 pos = camera.getPosition();
                double maxR = 0.85; // Keep comfortably inside the ball
                if (pos.normSq() > maxR * maxR) {
                    camera.setPosition(pos.normalize().scale(maxR));
                }
            }

            needsRender = true;
        }

        // Update particles
        switch (currentGeometry) {
            case "Euclidean":
                euclideanParticles.update(dt * 0.5);
                break;
            case "Hyperbolic":
                hyperbolicParticles.update(dt * 0.5);
                break;
            case "Spherical":
                sphericalParticles.update(dt * 0.5);
                break;
        }

        // Refresh scene with updated particle positions
        if (getActiveParticleSystem().size() > 0) {
            setupScene();
            needsRender = true;
        }
    }

    private void addParticle() {
        Vec3 pos = camera.getPosition();
        Vec3 vel = camera.getForward().scale(0.5); // Initial velocity in camera direction

        switch (currentGeometry) {
            case "Euclidean":
                euclideanParticles.addParticle(pos, vel, randomColor(), 0.05);
                break;
            case "Hyperbolic":
                try {
                    HyperboloidPoint hPos = hyperbolic.pointFromR3(pos);
                    hyperbolicParticles.addParticle(hPos, vel, randomColor(), 0.05);
                } catch (IllegalArgumentException e) {
                    System.out.println("Cannot add particle outside Poincaré ball");
                }
                break;
            case "Spherical":
                try {
                    SpherePoint sPos = spherical.pointFromR3(pos);
                    sphericalParticles.addParticle(sPos, vel, randomColor(), 0.05);
                } catch (IllegalArgumentException e) {
                    System.out.println("Cannot add particle: " + e.getMessage());
                }
                break;
        }

        setupScene();
        needsRender = true;
    }

    private void resetParticles() {
        euclideanParticles.clear();
        hyperbolicParticles.clear();
        sphericalParticles.clear();
        setupScene();
        needsRender = true;
    }

    private ParticleSystem<?> getActiveParticleSystem() {
        switch (currentGeometry) {
            case "Hyperbolic":
                return hyperbolicParticles;
            case "Spherical":
                return sphericalParticles;
            default:
                return euclideanParticles;
        }
    }

    private Vec3 randomColor() {
        return new Vec3(
                0.3 + Math.random() * 0.7,
                0.3 + Math.random() * 0.7,
                0.3 + Math.random() * 0.7);
    }

    private void renderFrame(GraphicsContext gc) {
        PixelWriter pw = image.getPixelWriter();

        // Render at lower resolution for preview
        for (int j = 0; j < HEIGHT; j += renderQuality) {
            for (int i = 0; i < WIDTH; i += renderQuality) {
                Vec3 color = rayTracer.renderPixel(i, j);

                int r = clamp((int) (color.x * 255), 0, 255);
                int g = clamp((int) (color.y * 255), 0, 255);
                int b = clamp((int) (color.z * 255), 0, 255);

                Color c = Color.rgb(r, g, b);

                // Fill block for preview quality
                for (int dj = 0; dj < renderQuality && j + dj < HEIGHT; dj++) {
                    for (int di = 0; di < renderQuality && i + di < WIDTH; di++) {
                        pw.setColor(i + di, j + dj, c);
                    }
                }
            }
        }

        gc.drawImage(image, 0, 0);

        // Draw HUD
        gc.setFill(Color.WHITE);
        gc.fillText("Geometry: " + currentGeometry, 10, 20);
        gc.fillText("Particles: " + getActiveParticleSystem().size(), 10, 35);
        gc.fillText(String.format("Camera: (%.2f, %.2f, %.2f)",
                camera.getPosition().x, camera.getPosition().y, camera.getPosition().z), 10, 50);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static void main(String[] args) {
        launch();
    }
}
