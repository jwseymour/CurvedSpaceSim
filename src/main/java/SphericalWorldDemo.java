import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import geometry.*;
import renderer.*;
import scene.objects.*;
import manifold.Vec3;

import java.util.ArrayList;
import java.util.List;

import java.awt.image.BufferedImage;

public class SphericalWorldDemo extends Application {
    private static final int VIEW_WIDTH = 800; // Game view width (pixels)
    private static final int VIEW_HEIGHT = 600; // Game view height (pixels)
    private static final int MAP_WIDTH = 400;
    private static final int MAP_HEIGHT = 400;

    private Camera2D camera;
    private GameRenderer renderer;
    private VisualizationOverlay overlay;

    private Space2D currentSpace;
    private List<SceneObject2D> objects = new ArrayList<>();

    // UI State
    private double sphereRadius = 10.0;
    private double pxPerUnit = 30.0; // Same as GameRenderer default

    // Input
    private boolean up, down, turnLeft, turnRight;

    private WritableImage viewImage;

    // Map Rotation State
    private double mapRotX = 0;
    private double mapRotY = 0;
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage stage) {
        // Init
        renderer = new GameRenderer();
        overlay = new VisualizationOverlay();

        // Default Space
        currentSpace = new SphericalSpace2D(sphereRadius);

        // Default Camera
        camera = new Camera2D(new Vec3(0, 0, sphereRadius), new Vec3(1, 0, 0), Math.toRadians(90));

        setupScene();

        // UI Layout
        BorderPane root = new BorderPane();
        VBox centerLayout = new VBox(10);
        centerLayout.setStyle("-fx-background-color: #222; -fx-padding: 10;");

        // Canvas for Game View
        Label viewLabel = new Label("Local 2D View (Game Window)");
        viewLabel.setTextFill(Color.WHITE);
        Canvas viewCanvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);

        // Canvas for Map View
        Label mapLabel = new Label("Global View (Window Extent)");
        mapLabel.setTextFill(Color.WHITE);
        Canvas mapCanvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);

        centerLayout.getChildren().addAll(viewLabel, viewCanvas, mapLabel, mapCanvas);
        root.setCenter(centerLayout);

        viewImage = new WritableImage(VIEW_WIDTH, VIEW_HEIGHT);

        // Controls
        VBox controls = new VBox(10);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #333;");

        ComboBox<String> spaceSelector = new ComboBox<>();
        spaceSelector.getItems().addAll("Spherical", "Euclidean");
        spaceSelector.setValue("Spherical");
        spaceSelector.setOnAction(e -> {
            if (spaceSelector.getValue().equals("Spherical")) {
                currentSpace = new SphericalSpace2D(sphereRadius);
                // Reset camera to surface of new sphere
                camera.setPosition(new Vec3(0, 0, sphereRadius));
            } else {
                currentSpace = new EuclideanSpace2D();
                camera.setPosition(new Vec3(0, 0, 0));
            }
            setupScene();
        });

        Label radiusLabel = new Label("Sphere Radius");
        radiusLabel.setTextFill(Color.WHITE);
        Slider radiusSlider = new Slider(5, 50, 10);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.valueProperty().addListener((obs, old, val) -> {
            sphereRadius = val.doubleValue();
            if (currentSpace instanceof SphericalSpace2D) {
                currentSpace = new SphericalSpace2D(sphereRadius);
                // Adjust simple objects
                setupScene();
            }
        });

        Label controlLabel = new Label("Controls:\nW/S: Move Up/Down (Y)\nQ/E: Move Left/Right (X)");
        controlLabel.setTextFill(Color.LIGHTGRAY);

        controls.getChildren().addAll(
                new Label("Space Type"), spaceSelector,
                radiusLabel, radiusSlider,
                controlLabel);
        root.setRight(controls);

        // Map Input Handling (Rotation)
        mapCanvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });
        mapCanvas.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            mapRotY += dx * 0.01; // Yaw
            mapRotX += dy * 0.01; // Pitch

            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        // Input Handling
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W:
                    up = true;
                    break;
                case S:
                    down = true;
                    break;
                case Q:
                    turnLeft = true; // reusing bool, now means Left Strafe
                    break;
                case E:
                    turnRight = true; // means Right Strafe
                    break;
                default:
                    break;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W:
                    up = false;
                    break;
                case S:
                    down = false;
                    break;
                case Q:
                    turnLeft = false;
                    break;
                case E:
                    turnRight = false;
                    break;
                default:
                    break;
            }
        });

        stage.setScene(scene);
        stage.setTitle("2D Spherical Game Demo");
        stage.show();

        // Loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(viewCanvas.getGraphicsContext2D(), mapCanvas.getGraphicsContext2D());
            }
        }.start();
    }

    private void setupScene() {
        objects.clear();
        double R = (currentSpace instanceof SphericalSpace2D) ? ((SphericalSpace2D) currentSpace).getRadius() : 10.0;

        // Add random circles
        objects.add(new GeodesicCircle(new Vec3(R / 2, 0, 0), 2.0, new Vec3(1, 0, 0)));
        objects.add(new GeodesicCircle(new Vec3(-R / 2, R / 2, 0), 1.5, new Vec3(0, 1, 0)));
        objects.add(new GeodesicCircle(new Vec3(0, -R / 2, 0), 1.0, new Vec3(0, 0, 1)));
        objects.add(new GeodesicCircle(new Vec3(0, R / 2, R / 2), 1.0, new Vec3(1, 1, 0)));
    }

    private void update() {
        double speed = 0.2;

        // "Up" key moves Camera "Forward" (which is up on local screen Y)
        // Camera Forward vector is defined how?
        // In 2D game, usually we might just translate X/Y in local tangent plane.
        // Let's implement strafe-like movement for simplicity.

        // Move Forward along Camera Forward
        if (up)
            camera.moveForward(speed, currentSpace);
        if (down)
            camera.moveForward(-speed, currentSpace);

        // Strafe Left/Right
        // Rotate camera forward 90 degrees to get "Right"
        // Move along that, then rotate back?
        // Or simply: rotate(90), move(speed), rotate(-90).
        if (turnRight) { // Strafe Right
            camera.rotate(-Math.PI / 2, currentSpace);
            camera.moveForward(speed, currentSpace);
            camera.rotate(Math.PI / 2, currentSpace);
        }
        if (turnLeft) { // Strafe Left
            camera.rotate(Math.PI / 2, currentSpace);
            camera.moveForward(speed, currentSpace);
            camera.rotate(-Math.PI / 2, currentSpace);
        }
    }

    private void render(GraphicsContext viewGc, GraphicsContext mapGc) {
        // 1. Render First Person (Game View)
        BufferedImage img = renderer.render(currentSpace, objects, camera, VIEW_WIDTH, VIEW_HEIGHT);
        PixelWriter pw = viewImage.getPixelWriter();
        int[] rgbArray = img.getRGB(0, 0, VIEW_WIDTH, VIEW_HEIGHT, null, 0, VIEW_WIDTH);
        for (int y = 0; y < VIEW_HEIGHT; y++) {
            for (int x = 0; x < VIEW_WIDTH; x++) {
                pw.setArgb(x, y, 0xFF000000 | rgbArray[y * VIEW_WIDTH + x]);
            }
        }
        viewGc.drawImage(viewImage, 0, 0);

        // 2. Render Map
        overlay.setRotation(mapRotX, mapRotY);

        BufferedImage mapImg = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = mapImg.createGraphics();

        // Pass Viewport info to Overlay
        overlay.drawTopDown(g2d, currentSpace, camera, objects, MAP_WIDTH, MAP_HEIGHT, VIEW_WIDTH, VIEW_HEIGHT,
                pxPerUnit);
        g2d.dispose();

        WritableImage mapFx = new WritableImage(MAP_WIDTH, MAP_HEIGHT);
        PixelWriter mapPw = mapFx.getPixelWriter();
        int[] mapRgb = mapImg.getRGB(0, 0, MAP_WIDTH, MAP_HEIGHT, null, 0, MAP_WIDTH);
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                mapPw.setArgb(x, y, 0xFF000000 | mapRgb[y * MAP_WIDTH + x]);
            }
        }
        mapGc.drawImage(mapFx, 0, 0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
