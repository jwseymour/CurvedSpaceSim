package sim;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sim.math.Vector2;
import sim.particle.Particle;
import sim.render.Camera;
import sim.render.Renderer;
import sim.surface.*;

public class Main extends Application {

    private static final int WIDTH=800, HEIGHT=600;

    private Camera camera = new Camera();
    private Renderer renderer = new Renderer(camera);

    private ParametricSurface surface;
    private Particle particle;

    private double lastX,lastY;
    private boolean dragging=false;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH,HEIGHT);
        root.setCenter(canvas);

        // ComboBox for selecting surfaces
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().addAll("Sphere","Torus","HyperbolicPlane");
        selector.setValue("Sphere");
        root.setTop(selector);

        // initialize surface and particle
        setSurface("Sphere");

        selector.setOnAction(e -> setSurface(selector.getValue()));

        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(e -> {
            if(e.getButton()==MouseButton.PRIMARY){
                lastX=e.getX(); lastY=e.getY();
                dragging=true;
            }
        });

        canvas.setOnMouseReleased(e -> dragging=false);

        canvas.setOnMouseDragged(e -> {
            if(dragging){
                double dx=e.getX()-lastX;
                double dy=e.getY()-lastY;
                camera.rotate(-dx*0.01,-dy*0.01);
                lastX=e.getX();
                lastY=e.getY();
            }
        });

        canvas.setOnScroll(e -> camera.zoom(Math.exp(-e.getDeltaY()*0.001)));

        new AnimationTimer(){
            @Override
            public void handle(long now){
                update();
                render(gc);
            }
        }.start();

        stage.setScene(new Scene(root));
        stage.setTitle("Multi-Surface Viewer");
        stage.show();
    }

    private void setSurface(String name){
        switch(name){
            case "Sphere":
                surface = new Sphere(200);
                particle = new Particle(surface, Math.PI/4,0,new Vector2(0,0.02));
                break;
            case "Torus":
                surface = new Torus(150,50);
                particle = new Particle(surface, Math.PI/4,0,new Vector2(0,0.02));
                break;
            case "HyperbolicPlane":
                surface = new HyperbolicPlane(300);
                particle = new Particle(surface,0,0,new Vector2(0,0.005));
                break;
        }
    }

    private void update(){ particle.update(1.0); }

    private void render(GraphicsContext gc){
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0,0,WIDTH,HEIGHT);

        renderer.drawSurface(gc,surface,30,60);
        renderer.drawParticle(gc,particle);
    }

    public static void main(String[] args){ launch(); }
}
