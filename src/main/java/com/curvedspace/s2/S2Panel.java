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
    private double angleX = 0;
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
        // Rotate world by angleX (around X axis) and angleY (around Y axis)
        // Mat4 rotation indices: 0=x, 1=y, 2=z
        Mat4 rotX = Mat4.rotation(1, 2, angleX); // Rotate Y towards Z (X axis rotation)
        Mat4 rotY = Mat4.rotation(2, 0, angleY); // Rotate Z towards X (Y axis rotation)
        Mat4 viewMat = rotX.mul(rotY);

        // Draw Sphere Outline
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));

        // Draw Grid Lines (Lat/Long)
        g2.setColor(new Color(50, 50, 50));
        drawCircle(g2, cx, cy, radius, new Vec3(1, 0, 0), new Vec3(0, 1, 0), viewMat); // Equator
        drawCircle(g2, cx, cy, radius, new Vec3(0, 1, 0), new Vec3(0, 0, 1), viewMat); // Meridian
        drawCircle(g2, cx, cy, radius, new Vec3(0, 0, 1), new Vec3(1, 0, 0), viewMat); // Meridian

        // Draw Objects
        g2.setColor(Color.RED);
        for (Vec3 obj : objectPositions) {
            drawPoint(g2, cx, cy, radius, obj, viewMat, 8);
        }

        // Camera at (0,0,1) in world space (North Pole)
        Vec3 camPos = new Vec3(0, 0, 1);
        g2.setColor(Color.CYAN);
        drawPoint(g2, cx, cy, radius, camPos, viewMat, 10);

        // Draw Rays from Camera
        g2.setColor(Color.YELLOW);
        // Fan of rays
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            // Tangent plane at (0,0,1) is XY plane.
            // Direction v = (cos a, sin a, 0)
            Vec3 dir = new Vec3(Math.cos(angle), Math.sin(angle), 0);
            drawGeodesic(g2, cx, cy, radius, camPos, dir, viewMat);
        }
    }

    private Vec3 transform(Vec3 v, Mat4 m) {
        Vec4 v4 = new Vec4(v.x, v.y, v.z, 0); // Direction/Point logic? Point on sphere: w=0 for rotation?
        // Our Mat4 is 4x4. If we put point in x,y,z,w=0, and rotate using 3D submatrix,
        // it works.
        // Mat4.rotation affects 2 axes.
        Vec4 res = m.mul(v4);
        return new Vec3(res.x, res.y, res.z);
    }

    private void drawPoint(Graphics2D g2, int cx, int cy, double r, Vec3 p, Mat4 view, int size) {
        Vec3 pt = transform(p, view);
        if (pt.z < 0) { // Backface cull? Or draw dim?
            g2.setColor(new Color(g2.getColor().getRed(), g2.getColor().getGreen(), g2.getColor().getBlue(), 50));
        } else {
            g2.setColor(new Color(g2.getColor().getRed(), g2.getColor().getGreen(), g2.getColor().getBlue(), 255));
        }
        int x = (int) (cx + pt.x * r);
        int y = (int) (cy - pt.y * r); // Y up in 3D, down in 2D
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private void drawCircle(Graphics2D g2, int cx, int cy, double r, Vec3 axis1, Vec3 axis2, Mat4 view) {
        // Draw circle in plane of axis1, axis2
        int steps = 50;
        int[] xPoints = new int[steps];
        int[] yPoints = new int[steps];

        for (int i = 0; i < steps; i++) {
            double t = i * 2 * Math.PI / steps;
            Vec3 p = axis1.scale(Math.cos(t)).plus(axis2.scale(Math.sin(t)));
            Vec3 pt = transform(p, view);
            xPoints[i] = (int) (cx + pt.x * r);
            yPoints[i] = (int) (cy - pt.y * r);
        }
        g2.drawPolyline(xPoints, yPoints, steps); // Close loop? drawPolygon
    }

    private void drawGeodesic(Graphics2D g2, int cx, int cy, double r, Vec3 start, Vec3 dir, Mat4 view) {
        // P(t) = P cos t + D sin t
        int steps = 100;
        int[] xPoints = new int[steps];
        int[] yPoints = new int[steps];

        for (int i = 0; i < steps; i++) {
            double t = i * 2 * Math.PI / steps; // Full circle
            Vec3 p = start.scale(Math.cos(t)).plus(dir.scale(Math.sin(t)));
            Vec3 pt = transform(p, view);
            xPoints[i] = (int) (cx + pt.x * r);
            yPoints[i] = (int) (cy - pt.y * r);
        }
        // Split into front/back segments?
        // Simple fallback: just draw it all.
        g2.drawPolyline(xPoints, yPoints, steps);
    }
}
