package sim.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sim.math.Vector2;
import sim.math.Vector3;
import sim.particle.Particle;
import sim.surface.ParametricSurface;

public class Renderer {

    private final Camera camera;

    public Renderer(Camera camera) { this.camera = camera; }

    public void drawSurface(GraphicsContext gc, ParametricSurface surface, int uSteps, int vSteps) {
        gc.setStroke(Color.LIGHTGRAY);

        for(int i=0;i<=uSteps;i++){
            double u = Math.PI*i/uSteps;
            for(int j=0;j<=vSteps;j++){
                double v = 2*Math.PI*j/vSteps;
                Vector3 p1 = surface.position(u,v);
                if(j>0){
                    Vector3 p0 = surface.position(u,2*Math.PI*(j-1)/vSteps);
                    drawLine(gc,p0,p1);
                }
                if(i>0){
                    Vector3 p0 = surface.position(Math.PI*(i-1)/uSteps,v);
                    drawLine(gc,p0,p1);
                }
            }
        }
    }

    public void drawParticle(GraphicsContext gc, Particle particle) {
        gc.setFill(Color.RED);
        Vector3 pos = particle.getPosition();
        Vector2 screen = camera.project(pos, (int)gc.getCanvas().getWidth(), (int)gc.getCanvas().getHeight());
        gc.fillOval(screen.x-5,screen.y-5,10,10);
    }

    private void drawLine(GraphicsContext gc, Vector3 a, Vector3 b){
        Vector2 p1 = camera.project(a,(int)gc.getCanvas().getWidth(),(int)gc.getCanvas().getHeight());
        Vector2 p2 = camera.project(b,(int)gc.getCanvas().getWidth(),(int)gc.getCanvas().getHeight());
        gc.strokeLine(p1.x,p1.y,p2.x,p2.y);
    }
}
