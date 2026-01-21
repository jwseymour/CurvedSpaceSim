package sim.render;

import sim.math.Vector2;
import sim.math.Vector3;

public class Camera {

    private double radius = 500;
    private double theta = Math.PI/4;
    private double phi = Math.PI/4;
    private Vector3 target = new Vector3(0,0,0);

    public void rotate(double dTheta, double dPhi) {
        theta += dTheta;
        phi += dPhi;
        phi = Math.max(0.01, Math.min(Math.PI-0.01, phi));
    }

    public void zoom(double factor) {
        radius *= factor;
        radius = Math.max(50, Math.min(2000, radius));
    }

    public Vector3 getPosition() {
        double x = target.x + radius * Math.sin(phi) * Math.cos(theta);
        double y = target.y + radius * Math.sin(phi) * Math.sin(theta);
        double z = target.z + radius * Math.cos(phi);
        return new Vector3(x, y, z);
    }

    public Vector2 project(Vector3 worldPoint, int screenWidth, int screenHeight) {
        Vector3 camPos = getPosition();
        Vector3 forward = target.subtract(camPos).normalize();
        Vector3 up = new Vector3(0,0,1);
        Vector3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Vector3 diff = worldPoint.subtract(camPos);
        double x = diff.dot(right);
        double y = diff.dot(up);
        double z = diff.dot(forward);

        double fov = 1.0;
        double sx = (x/z) * screenWidth/2 * fov + screenWidth/2;
        double sy = (-y/z) * screenHeight/2 * fov + screenHeight/2;
        return new Vector2(sx, sy);
    }
}
