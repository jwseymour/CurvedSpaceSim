package renderer;

import geometry.Space2D;
import manifold.Vec3;
import rays.Ray2D;

public class Camera2D {
    private Vec3 position;
    private Vec3 forward;
    private double fov; // in radians

    public Camera2D(Vec3 position, Vec3 forward, double fov) {
        this.position = position;
        this.forward = forward.normalize();
        this.fov = fov;
    }

    public void setPosition(Vec3 p) {
        this.position = p;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setForward(Vec3 f) {
        this.forward = f.normalize();
    }

    public Vec3 getForward() {
        return forward;
    }

    public double getFov() {
        return fov;
    }

    public void setFov(double fov) {
        this.fov = fov;
    }

    /**
     * @return The angle in radians relative to X-axis (for 2D Euclidean).
     */
    public double getAngle() {
        return Math.atan2(forward.y, forward.x);
    }

    public void moveForward(double distance, Space2D space) {
        Ray2D currentRay = new Ray2D(position, forward);
        Ray2D nextRay = space.advance(currentRay, distance);
        this.position = nextRay.p;
        this.forward = nextRay.v;
    }

    public void rotate(double angle, Space2D space) {
        Vec3 right = space.getOrthogonal(position, forward);
        // Rotate 'forward' towards 'right' by angle
        // forward_new = forward * cos + right * sin
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        this.forward = forward.scale(c).add(right.scale(s)).normalize();
    }

    public Ray2D getRay(double ndc, Space2D space) {
        // ndc is [-1, 1] relative to center of view
        double angle = ndc * (fov * 0.5);

        Vec3 right = space.getOrthogonal(position, forward);
        double c = Math.cos(angle);
        double s = Math.sin(angle);

        Vec3 rayDir = forward.scale(c).add(right.scale(s)).normalize();

        return new Ray2D(position, rayDir);
    }
}
