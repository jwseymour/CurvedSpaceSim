package renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.List;

import geometry.Space2D;
import geometry.SphericalSpace2D;
import geometry.EuclideanSpace2D;
import scene.objects.SceneObject2D;
import manifold.Vec3;

public class GameRenderer {

    public BufferedImage render(Space2D space, List<SceneObject2D> objects, Camera2D camera, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Background
        g2d.setColor(new Color(20, 20, 30)); // Deep Space / Dark BG
        g2d.fillRect(0, 0, width, height);

        // Center of screen
        int cx = width / 2;
        int cy = height / 2;

        // Scale: How many pixels per unit?
        // Let's say 20 pixels per unit.
        // If R=10, then 10 units = 200 pixels.
        double pxPerUnit = 30.0;

        if (space instanceof EuclideanSpace2D) {
            renderEuclidean(g2d, objects, camera, cx, cy, pxPerUnit);
        } else if (space instanceof SphericalSpace2D) {
            renderSpherical(g2d, objects, (SphericalSpace2D) space, camera, cx, cy, pxPerUnit, width, height);
        }

        g2d.dispose();
        return image;
    }

    private void renderEuclidean(Graphics2D g, List<SceneObject2D> objects, Camera2D camera, int cx, int cy,
            double scale) {
        Vec3 camPos = camera.getPosition();
        double camRot = camera.getAngle(); // If we assume 2D rotation

        for (SceneObject2D obj : objects) {
            Vec3 pos = obj.getPosition();

            // Relative position
            double dx = pos.x - camPos.x;
            double dy = pos.y - camPos.y;

            // Rotate by camera angle (Inverse rotation to align camera up)
            // If camera angle is 0, we look +Y? Or +X?
            // Let's assume standard math conventions: +X is 0.
            // But usually Up is +Y.
            // If Camera Angle = alpha. Point P.
            // P_local = R(-alpha) * (P - C)

            double rDx = dx * Math.cos(-camRot) - dy * Math.sin(-camRot);
            double rDy = dx * Math.sin(-camRot) + dy * Math.cos(-camRot);

            // Screen coords (Center origin)
            // Screen X = cx + rDx * scale
            // Screen Y = cy - rDy * scale (Y flip)

            int sx = cx + (int) (rDx * scale);
            int sy = cy - (int) (rDy * scale);

            // Draw Object (Circle)
            // Nominal radius in pixels
            int r = (int) (obj.getColor() != null ? 5 : 5); // Default size

            g.setColor(new Color((int) (obj.getColor().x * 255), (int) (obj.getColor().y * 255),
                    (int) (obj.getColor().z * 255)));
            g.fillOval(sx - r, sy - r, r * 2, r * 2);
        }
    }

    private void renderSpherical(Graphics2D g, List<SceneObject2D> objects, SphericalSpace2D space, Camera2D camera,
            int cx, int cy, double scale, int width, int height) {
        // 1. Transform World so Camera is at "North Pole" (0, 0, R) (or appropriate
        // pole)
        // Camera Position C.
        // We want a rotation R such that R * C = (0, 0, R).

        // For simplicity, let's keep it robust.
        // The camera defines a "local tangent plane".
        // We want to project points onto this plane.
        // This is equivalent to rotating the sphere so C is at the "top" and projecting
        // orthographically (discarding Z).

        Vec3 camPos = camera.getPosition(); // On sphere
        // We want to map this to an azimuthal projection (e.g. orthographic or
        // stereographic)
        // centered at the camera's position. (Normalized)
        Vec3 up = camPos.normalize();

        // Forward = Camera Forward Vector (Tangent to surface)
        Vec3 fwd = camera.getForward().normalize();

        // Right = fwd x up (Tangent to surface)
        Vec3 right = fwd.cross(up).normalize();

        // Matrix M = [Right, Fwd, Up] (Rows or Cols?)
        // We want to project Point P into this basis.
        // P_local_x = P dot Right
        // P_local_y = P dot Fwd
        // P_local_z = P dot Up

        for (SceneObject2D obj : objects) {
            Vec3 pos = obj.getPosition();

            // Check if object is in upper hemisphere relative to camera
            // If P dot Up < 0, it's on the other side of the world (horizon culling)
            double dot = pos.dot(up);
            if (dot < 0)
                continue; // Cull back-face

            // Project
            double px = pos.dot(right);
            double py = pos.dot(fwd);

            // Note: px corresponds to "Right" on screen (X)
            // py corresponds to "Forward" on screen (Y, Up)

            // Scale to screen
            int sx = cx + (int) (px * scale);
            int sy = cy - (int) (py * scale); // Y flip

            // Cull if off screen
            if (sx < -50 || sx > width + 50 || sy < -50 || sy > height + 50)
                continue;

            // Draw
            int r = 6;
            g.setColor(new Color((int) (obj.getColor().x * 255), (int) (obj.getColor().y * 255),
                    (int) (obj.getColor().z * 255)));
            g.fillOval(sx - r, sy - r, r * 2, r * 2);
        }
    }
}
