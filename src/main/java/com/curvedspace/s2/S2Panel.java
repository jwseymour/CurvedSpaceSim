package com.curvedspace.s2;

import com.curvedspace.math.Mat4;
import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class S2Panel extends JPanel {
    // Initial rotation: Z is UP.
    // To see Equator horizontal, we need to map Z to Screen Y (Up).
    // Standard View: Y is Up.
    // So we rotate -90 deg around X.
    private double angleX = -Math.PI / 2.0;
    private double angleY = 0;
    private int lastX, lastY;

    private final List<Vec3> objectPositions = new ArrayList<>();

    public S2Panel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;
                angleY += dx * 0.01;
                angleX += dy * 0.01;
                lastX = e.getX();
                lastY = e.getY();
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // Add some random objects on sphere
        objectPositions.add(new Vec3(1, 0, 0));
        objectPositions.add(new Vec3(0, 1, 0));
        objectPositions.add(new Vec3(0, 0, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        double radius = Math.min(w, h) * 0.4;

        // View Rotation Matrix
        Mat4 rotX = Mat4.rotation(1, 2, angleX); // Rotate Y towards Z (Pitch around X)
        // We want to rotate around Z (Yaw) for the horizontal control, since we are
        // Z-up.
        // Rotation in X-Y plane (indices 0, 1).
        Mat4 rotZ = Mat4.rotation(0, 1, -angleY);

        // Apply Yaw (Z) then Pitch (X)
        Mat4 viewMat = rotX.mul(rotZ);

        // --- DRAW BACKGROUND SPHERE ---
        // Fill clear/black
        g2.setColor(new Color(10, 10, 15));
        g2.fillOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));

        // --- DRAW GROUND (Southern Hemisphere Z < 0) ---
        // We can't easily fill *just* the hemisphere in 2D without 3D logic.
        // But we can draw the grid densely.
        // Draw Lat/Long for Southern Hemisphere
        g2.setColor(new Color(20, 100, 20)); // Dark Green Lines
        // Latitudes: -90 to 0
        for (int i = 0; i <= 9; i++) {
            double lat = -Math.PI / 2 + (i * Math.PI / 2 / 9);
            // Circle at z = sin(lat), radius = cos(lat)
            double z = Math.sin(lat);
            double rCircle = Math.cos(lat);
            if (i == 9)
                rCircle = 1.0; // Equator exact

            drawCircle3D(g2, cx, cy, radius, z, rCircle, viewMat, (i == 9));
        }
        // Longitudes for Southern Hemisphere
        // We can just draw full longitudes but clip? No, too complex.
        // Let's just draw full longitude circles but colored differently?
        // Just drawing the grid on the whole sphere is easier, but let's try to
        // distinctive bottom.
        // Let's draw standard grid faint everywhere, and extra green in south?

        // Draw Full faint grid
        g2.setColor(new Color(40, 40, 40));
        // Equator
        // drawCircle(g2, cx, cy, radius, new Vec3(1, 0, 0), new Vec3(0, 1, 0),
        // viewMat);

        // --- SCENE OBJECTS ---
        // Character "Above" Equator (Z > 0)
        // (1, 0, 0.2) normalized
        Vec3 charPos = new Vec3(1, 0, 0.2).normalize();
        // House at Equator defined by antipode (-1, 0, 0)
        Vec3 housePos = new Vec3(-1, 0, 0);

        // --- DRAW RAYS (Upper Hemisphere) ---
        g2.setColor(new Color(255, 255, 0, 150)); // Yellow translucent
        int numRays = 24;
        for (int i = 0; i < numRays; i++) {
            // Fan of rays from Character.
            // Tangent plane at charPos.
            // Basis vectors for tangent plane?
            // If charPos is approx (1,0,0), tangent plane is YZ.
            // We want rays generally going "Up" and "Forward/Backward"?
            // Let's form an orthonormal basis (u, v) for tangent plane at charPos.
            // Normal = charPos.
            // Up = (0, 0, 1).
            // If charPos is exactly (0,0,1), this fails, but it's not.
            Vec3 up = new Vec3(0, 0, 1);
            Vec3 right = up.cross(charPos).normalize();
            Vec3 forward = charPos.cross(right).normalize(); // This 'forward' points roughly Up along meridian.

            // Generate direction in tangent plane.
            // We want to fan out.
            double angle = (i * Math.PI / (numRays - 1)); // 0 to PI
            // 0 -> Right, PI/2 -> Forward (Up), PI -> Left
            // We want circle in plane spanning Right and Forward.
            // dir = right * cos(angle) + forward * sin(angle)

            Vec3 dir = right.scale(-Math.cos(angle)).plus(forward.scale(Math.sin(angle))).normalize();

            // Draw geodesic from charPos with direction dir
            drawGeodesicArc(g2, cx, cy, radius, charPos, dir, viewMat, Color.YELLOW);
        }

        // --- DRAW CHARACTERS / BILLBOARDS ---
        drawBillboard(g2, cx, cy, radius, housePos, viewMat, "🏠", 30);
        drawBillboard(g2, cx, cy, radius, charPos, viewMat, "😎", 25);

        // Draw Equator Line distinctly
        g2.setColor(Color.GREEN);
        drawCircle(g2, cx, cy, radius, new Vec3(1, 0, 0), new Vec3(0, 1, 0), viewMat);

        // Sphere Outline
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));
    }

    private Vec3 transform(Vec3 v, Mat4 m) {
        Vec4 v4 = new Vec4(v.x, v.y, v.z, 0);
        Vec4 res = m.mul(v4);
        return new Vec3(res.x, res.y, res.z);
    }

    private void drawPoint(Graphics2D g2, int cx, int cy, double r, Vec3 p, Mat4 view, int size, Color c) {
        Vec3 pt = transform(p, view);
        int alpha = (pt.z > 0) ? 255 : 50;
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));

        int x = (int) (cx + pt.x * r);
        int y = (int) (cy - pt.y * r);
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private void drawBillboard(Graphics2D g2, int cx, int cy, double r, Vec3 p, Mat4 view, String text, int fontSize) {
        Vec3 pt = transform(p, view);
        // Fade if on back side
        // Note: transforming logic. For display Z is depth?
        // Our viewMat maps World to Screen.
        // Usually Screen Z is depth.
        // Let's use alpha based on Z.

        int alpha = (pt.z > 0) ? 255 : 60;
        g2.setColor(new Color(255, 255, 255, alpha));
        g2.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, fontSize));

        int x = (int) (cx + pt.x * r);
        int y = (int) (cy - pt.y * r);

        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);

        g2.drawString(text, x - tw / 2, y + fontSize / 3);
    }

    private void drawCircle(Graphics2D g2, int cx, int cy, double r, Vec3 axis1, Vec3 axis2, Mat4 view) {
        int steps = 60;
        int[] xPoints = new int[steps];
        int[] yPoints = new int[steps];

        for (int i = 0; i < steps; i++) {
            double t = i * 2 * Math.PI / steps;
            Vec3 p = axis1.scale(Math.cos(t)).plus(axis2.scale(Math.sin(t)));
            Vec3 pt = transform(p, view);
            xPoints[i] = (int) (cx + pt.x * r);
            yPoints[i] = (int) (cy - pt.y * r);
        }
        g2.drawPolygon(xPoints, yPoints, steps);
    }

    // Draw horizontal circle at specific Z
    private void drawCircle3D(Graphics2D g2, int cx, int cy, double r, double z, double rCircle, Mat4 view,
            boolean isEquator) {
        int steps = 60;
        int[] xPoints = new int[steps];
        int[] yPoints = new int[steps];

        for (int i = 0; i < steps; i++) {
            double t = i * 2 * Math.PI / steps;
            // Circle in XY plane at height Z
            Vec3 p = new Vec3(rCircle * Math.cos(t), rCircle * Math.sin(t), z);
            Vec3 pt = transform(p, view);
            xPoints[i] = (int) (cx + pt.x * r);
            yPoints[i] = (int) (cy - pt.y * r);
        }
        g2.drawPolygon(xPoints, yPoints, steps);
    }

    private void drawGeodesicArc(Graphics2D g2, int cx, int cy, double r, Vec3 start, Vec3 dir, Mat4 view, Color c) {
        // P(t) = P cos t + D sin t
        int steps = 100;
        List<Integer> xPoints = new ArrayList<>();
        List<Integer> yPoints = new ArrayList<>();

        for (int i = 0; i <= steps; i++) {
            double t = i * Math.PI / steps; // 0 to PI check (Max possible)
            // But we actually want to continue until we hit Z < 0.
            // Let's use a slightly longer range just in case, or adaptive step.
            // Simple: just check Z.

            Vec3 p = start.scale(Math.cos(t)).plus(dir.scale(Math.sin(t)));

            if (p.z < -0.01) { // Stop if we go below equator (allow tiny epsilon)
                break;
            }

            Vec3 pt = transform(p, view);
            xPoints.add((int) (cx + pt.x * r));
            yPoints.add((int) (cy - pt.y * r));
        }

        if (xPoints.size() > 1) {
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100)); // faint
            int[] xArr = xPoints.stream().mapToInt(i -> i).toArray();
            int[] yArr = yPoints.stream().mapToInt(i -> i).toArray();
            g2.drawPolyline(xArr, yArr, xArr.length);
        }
    }
}
