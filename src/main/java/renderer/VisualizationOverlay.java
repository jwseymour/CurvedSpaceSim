package renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import scene.objects.SceneObject2D;
import scene.objects.GeodesicCircle;
import geometry.Space2D;
import geometry.SphericalSpace2D;
import geometry.EuclideanSpace2D;
import manifold.Vec3;
import rays.Ray2D;

/**
 * Renders debug visualizations (Top-down view of the manifold).
 * Supports both flattened 2D view and rotatable 3D view for S2.
 */
public class VisualizationOverlay {

    // Rotation state (in radians)
    private double rotX = 0; // Pitch
    private double rotY = 0; // Yaw

    public void setRotation(double rx, double ry) {
        this.rotX = rx;
        this.rotY = ry;
    }

    /**
     * @param viewWidth  The width of the "Game View" in pixels.
     * @param viewHeight The height of the "Game View" in pixels.
     * @param pxPerUnit  The scale factor used in GameRenderer.
     */
    public void drawTopDown(Graphics2D g, Space2D space, Camera2D camera, List<SceneObject2D> objects, int width,
            int height, int viewWidth, int viewHeight, double pxPerUnit) {
        // Clear background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, width, height);

        int cx = width / 2;
        int cy = height / 2;
        double scale = Math.min(width, height) / 2.5;

        // Draw Manifold
        if (space instanceof SphericalSpace2D) {
            drawSphereWireframe(g, cx, cy, scale);
        }

        // Draw objects
        for (SceneObject2D obj : objects) {
            if (obj instanceof GeodesicCircle) {
                GeodesicCircle circle = (GeodesicCircle) obj;
                Vec3 center = circle.getCenter();
                double radius = circle.getRadius();
                drawPointOnSphere(g, center, cx, cy, scale, (int) radius + 4);
            }
        }

        // --- NEW: Draw Viewport on Sphere ---
        // The Viewport defines a rectangular region in the "Tangent Plane" (Game View).
        // Corners in local Tangent Space (relative to Camera):
        double halfW = (viewWidth / 2.0) / pxPerUnit;
        double halfH = (viewHeight / 2.0) / pxPerUnit;

        // 4 Corners: (+W, +H), (-W, +H), (-W, -H), (+W, -H)
        Vec3[] localCorners = {
                new Vec3(halfW, halfH, 0),
                new Vec3(-halfW, halfH, 0),
                new Vec3(-halfW, -halfH, 0),
                new Vec3(halfW, -halfH, 0)
        };

        // Transform Local Corners to World Space
        Vec3[] worldCorners = new Vec3[4];
        Vec3 camPos = camera.getPosition();

        // We need the Inverse of the Projection mapping used in GameRenderer.
        // GameRenderer: P_local = P dot Basis.
        // Inverse: P_world = C + x*Right + y*Fwd (approximated for plane)
        // BUT for Sphere, we map (x,y) on tangent plane back to surface via Exponential
        // Map?
        // Or simply: x -> Right, y -> Fwd. And we project onto sphere?

        // Let's use the local basis vectors again.
        Vec3 up = camPos.normalize();
        Vec3 fwd = camera.getForward().normalize();
        Vec3 right = fwd.cross(up).normalize();

        // For S2, "straight lines" on the screen (GameRenderer) are likely great
        // circles passing through center?
        // GameRenderer uses: P_screen_x = (P-C).Right.
        // This is effectively an Orthographic projection.
        // Inverse Orthographic:
        // P = C + x*Right + y*Fwd + z*Up
        // We know P is on sphere, so |P|=R.
        // (C + xR + yF)^2 + z^2 = R^2 ?? No, complex.

        // Let's visualize the "Tangent Rectangle" floating there,
        // OR project it onto the sphere surface.
        // Let's project it onto the sphere: P = normalize(C + x*Right + y*Fwd) * R

        double R = (space instanceof SphericalSpace2D) ? ((SphericalSpace2D) space).getRadius() : 10.0;

        for (int i = 0; i < 4; i++) {
            // Unproject
            Vec3 tangentOffset = right.scale(localCorners[i].x).add(fwd.scale(localCorners[i].y));
            // P_tangent = C + Offset
            Vec3 pTangent = camPos.add(tangentOffset);

            // Project to surface
            // For Euclidean, it's just pTangent (z=0 implicit?)
            if (space instanceof EuclideanSpace2D) {
                worldCorners[i] = pTangent; // Assuming Z=0 plane
            } else {
                worldCorners[i] = pTangent.normalize().scale(R);
            }
        }

        // Draw the Viewport Quad
        g.setColor(new Color(0, 255, 255, 150)); // Cyan
        for (int i = 0; i < 4; i++) {
            drawLine3D(g, worldCorners[i], worldCorners[(i + 1) % 4], cx, cy, scale);
        }

        // Draw Camera Center
        g.setColor(Color.YELLOW);
        drawPointOnSphere(g, camPos, cx, cy, scale, 6);
    }

    private void drawSphereWireframe(Graphics2D g, int cx, int cy, double scale) {
        g.setColor(new Color(60, 60, 60));

        // Lat/Lon lines
        int steps = 36;
        // Longitude lines
        for (int i = 0; i < 12; i++) {
            double lon = i * (Math.PI / 6);
            for (int j = 0; j < steps; j++) {
                double lat1 = (j / (double) steps) * Math.PI - Math.PI / 2;
                double lat2 = ((j + 1) / (double) steps) * Math.PI - Math.PI / 2;

                Vec3 p1 = sphericalToCart(10.0, lat1, lon);
                Vec3 p2 = sphericalToCart(10.0, lat2, lon);
                drawLine3D(g, p1, p2, cx, cy, scale);
            }
        }

        // Latitude lines
        for (int i = 1; i < 6; i++) {
            double lat = i * (Math.PI / 6) - Math.PI / 2;
            for (int j = 0; j < steps; j++) {
                double lon1 = (j / (double) steps) * 2 * Math.PI;
                double lon2 = ((j + 1) / (double) steps) * 2 * Math.PI;

                Vec3 p1 = sphericalToCart(10.0, lat, lon1);
                Vec3 p2 = sphericalToCart(10.0, lat, lon2);
                drawLine3D(g, p1, p2, cx, cy, scale);
            }
        }
    }

    private Vec3 sphericalToCart(double R, double lat, double lon) {
        // x = R cos lat cos lon
        // y = R cos lat sin lon
        // z = R sin lat
        // Swapped for our coordinate system: z is up (lat), x/y are plane.
        // Let's assume standard:
        // x, y on plane, z up.
        // lat -> pitch from xy plane (z)
        // lon -> yaw in xy plane
        double rXY = R * Math.cos(lat);
        double x = rXY * Math.cos(lon);
        double y = rXY * Math.sin(lon);
        double z = R * Math.sin(lat);
        return new Vec3(x, y, z);
    }

    private void drawGeodesic(Graphics2D g, Ray2D startRay, Space2D space, int steps, int cx, int cy, double scale) {
        Ray2D current = startRay;
        Vec3 prevP = current.p;

        double R = (space instanceof SphericalSpace2D) ? ((SphericalSpace2D) space).getRadius() : 10.0;
        double totalDist = R * 2.0;
        double stepSize = totalDist / steps;

        for (int i = 0; i < steps; i++) {
            current = space.advance(current, stepSize);
            Vec3 nextP = current.p;
            drawLine3D(g, prevP, nextP, cx, cy, scale);
            prevP = nextP;
        }
    }

    // Draw a point in 3D
    private void drawPointOnSphere(Graphics2D g, Vec3 p, int cx, int cy, double scale, int size) {
        Point pt = project(p, cx, cy, scale);
        // Simple Z-check?
        // Allow seeing through for now, or fade back.
        Vec3 rotated = rotate(p);
        if (rotated.z > 0) {
            g.fillOval(pt.x - size / 2, pt.y - size / 2, size, size);
        } else {
            // Backface
            g.drawOval(pt.x - size / 2, pt.y - size / 2, size, size);
        }
    }

    private void drawLine3D(Graphics2D g, Vec3 p1, Vec3 p2, int cx, int cy, double scale) {
        Point pt1 = project(p1, cx, cy, scale);
        Point pt2 = project(p2, cx, cy, scale);
        g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
    }

    // projects 3D point to 2D screen using current rotation
    private Point project(Vec3 v, int cx, int cy, double scale) {
        // 1. Rotate
        Vec3 r = rotate(v);

        // 2. Project (Orthographic for now)
        // Ensure coordinate system matches screen
        // x -> right, y -> up (screen y down)

        // Assume world coords: Z up? Or whatever.
        // Let's render X/Z plane to screen X/Y?

        // Using rotated coordinates directly:
        // x screen = r.x * scale / R
        // y screen = -r.y * scale / R (invert y)

        // Normalize by Radius roughly (or just scale factor)
        // Scale is already pixels-per-radius-ish?
        // User passed scale = min(w,h)/2.5. So R maps to scale.
        double R = 10.0; // Nominal radius for projection scale

        int sx = cx + (int) ((r.x / R) * scale);
        int sy = cy - (int) ((r.y / R) * scale); // Minus for screen Y
        return new Point(sx, sy);
    }

    private Vec3 rotate(Vec3 v) {
        // Rotate around X (Pitch)
        double y1 = v.y * Math.cos(rotX) - v.z * Math.sin(rotX);
        double z1 = v.y * Math.sin(rotX) + v.z * Math.cos(rotX);
        double x1 = v.x;

        // Rotate around Y (Yaw)
        double x2 = x1 * Math.cos(rotY) + z1 * Math.sin(rotY);
        double z2 = -x1 * Math.sin(rotY) + z1 * Math.cos(rotY);
        double y2 = y1;

        return new Vec3(x2, y2, z2);
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
