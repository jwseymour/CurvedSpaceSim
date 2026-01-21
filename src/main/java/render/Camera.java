package render;

import manifold.Vec3;

/**
 * Camera for viewing inside a curved manifold.
 * 
 * The camera defines:
 * - Position: where the viewer is located (in R³ visualization coordinates)
 * - Orientation: an orthonormal frame (right, up, forward) in the tangent space
 * - Field of view and aspect ratio for projection
 */
public class Camera {

    private Vec3 position;

    // Orthonormal basis for the camera's tangent space
    private Vec3 right;
    private Vec3 up;
    private Vec3 forward;

    private double fovRadians;
    private double aspectRatio;

    /**
     * Create a camera at the origin looking along -Z (standard OpenGL convention).
     */
    public Camera() {
        this.position = Vec3.ZERO;
        this.forward = new Vec3(0, 0, -1);
        this.up = Vec3.Y;
        this.right = Vec3.X;
        this.fovRadians = Math.toRadians(60);
        this.aspectRatio = 16.0 / 9.0;
    }

    /**
     * Create a camera with specified position and look direction.
     */
    public Camera(Vec3 position, Vec3 lookDir, Vec3 worldUp) {
        this.position = position;
        this.forward = lookDir.normalize();
        this.right = this.forward.cross(worldUp).normalize();
        this.up = this.right.cross(this.forward);
        this.fovRadians = Math.toRadians(60);
        this.aspectRatio = 16.0 / 9.0;
    }

    public void setPosition(Vec3 pos) {
        this.position = pos;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setFov(double degrees) {
        this.fovRadians = Math.toRadians(degrees);
    }

    public void setAspectRatio(double ratio) {
        this.aspectRatio = ratio;
    }

    /**
     * Look at a target point from the current position.
     */
    public void lookAt(Vec3 target, Vec3 worldUp) {
        this.forward = target.sub(position).normalize();
        this.right = this.forward.cross(worldUp).normalize();
        this.up = this.right.cross(this.forward);
    }

    /**
     * Set orientation using Euler angles (yaw, pitch in radians).
     */
    public void setOrientation(double yaw, double pitch) {
        // Yaw rotates around Y axis, pitch rotates around X axis
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        double cp = Math.cos(pitch), sp = Math.sin(pitch);

        // Forward direction from angles
        this.forward = new Vec3(
                -sy * cp,
                sp,
                -cy * cp).normalize();

        // Recompute basis
        Vec3 worldUp = Vec3.Y;
        this.right = this.forward.cross(worldUp).normalize();
        if (this.right.norm() < 1e-6) {
            // Looking straight up or down
            this.right = Vec3.X;
        }
        this.up = this.right.cross(this.forward);
    }

    /**
     * Rotate the camera by yaw and pitch deltas.
     */
    public void rotate(double deltaYaw, double deltaPitch) {
        // Get current angles
        double pitch = Math.asin(Math.max(-1, Math.min(1, forward.y)));
        double yaw = Math.atan2(-forward.x, -forward.z);

        // Apply deltas
        yaw += deltaYaw;
        pitch = Math.max(-Math.PI / 2 + 0.01, Math.min(Math.PI / 2 - 0.01, pitch + deltaPitch));

        setOrientation(yaw, pitch);
    }

    /**
     * Move the camera forward/backward and strafe left/right.
     */
    public void move(double forwardAmount, double rightAmount, double upAmount) {
        position = position.add(forward.scale(forwardAmount))
                .add(right.scale(rightAmount))
                .add(up.scale(upAmount));
    }

    /**
     * Get a ray for the given pixel coordinates.
     * 
     * @param u Horizontal coordinate in [0, 1] (0 = left, 1 = right)
     * @param v Vertical coordinate in [0, 1] (0 = top, 1 = bottom)
     * @return A GeodesicRay starting at the camera position
     */
    public GeodesicRay getRay(double u, double v) {
        // Convert to centered coordinates [-1, 1]
        double nx = (2.0 * u - 1.0) * aspectRatio;
        double ny = 1.0 - 2.0 * v;

        // Scale by FOV
        double tanFov = Math.tan(fovRadians / 2);
        nx *= tanFov;
        ny *= tanFov;

        // Combine basis vectors to get ray direction
        Vec3 direction = forward.add(right.scale(nx)).add(up.scale(ny)).normalize();

        return new GeodesicRay(position, direction);
    }

    public Vec3 getForward() {
        return forward;
    }

    public Vec3 getUp() {
        return up;
    }

    public Vec3 getRight() {
        return right;
    }
}
