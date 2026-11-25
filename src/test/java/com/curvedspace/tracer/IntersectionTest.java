package com.curvedspace.tracer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.curvedspace.geometry.Ray;
import com.curvedspace.geometry.S3Space;
import com.curvedspace.math.Vec3;
import com.curvedspace.math.Vec4;
import org.junit.jupiter.api.Test;

class IntersectionTest {
    private static final Vec3 WHITE = new Vec3(1, 1, 1);
    private static final Vec4 POLE = new Vec4(0, 0, 0, 1);

    // Camera position 0.3 radians off the x axis, in the x-w plane. Heading
    // "towards" takes the short way to POLE, "away" takes the long way round.
    private static final double START = 0.3;
    private static final Vec4 ORIGIN = new Vec4(Math.cos(START), 0, 0, Math.sin(START));
    private static final Vec4 TOWARDS = new Vec4(-Math.sin(START), 0, 0, Math.cos(START));
    private static final Vec4 AWAY = TOWARDS.scale(-1);

    private static final double SHORT_WAY = Math.PI / 2 - START;
    private static final double LONG_WAY = 2 * Math.PI - SHORT_WAY;

    private static S3Triangle triangleAtPole() {
        double e = 0.05;
        return new S3Triangle(
                new Vec4(0, e, 0, 1).normalize(),
                new Vec4(0, -e, e, 1).normalize(),
                new Vec4(0, -e, -e, 1).normalize(),
                WHITE);
    }

    @Test
    void sphereIsHitAtItsNearSurface() {
        double radius = 0.05;
        Sphere sphere = new Sphere(POLE, radius, WHITE);

        Intersection hit = sphere.intersect(new Ray(ORIGIN, TOWARDS));

        assertNotNull(hit);
        assertEquals(SHORT_WAY - radius, hit.distance, 1e-9);
        assertEquals(radius, S3Space.distance(hit.point, POLE), 1e-9);
    }

    @Test
    void sphereIsVisibleTheLongWayRound() {
        double radius = 0.05;
        Sphere sphere = new Sphere(POLE, radius, WHITE);

        Intersection hit = sphere.intersect(new Ray(ORIGIN, AWAY));

        assertNotNull(hit);
        assertEquals(LONG_WAY - radius, hit.distance, 1e-9);
    }

    @Test
    void sphereOffToTheSideIsMissed() {
        Sphere sphere = new Sphere(new Vec4(0, 1, 0, 0), 0.05, WHITE);

        assertNull(sphere.intersect(new Ray(ORIGIN, TOWARDS)));
    }

    @Test
    void triangleIsHitTheShortWay() {
        Intersection hit = triangleAtPole().intersect(new Ray(ORIGIN, TOWARDS));

        assertNotNull(hit);
        assertEquals(SHORT_WAY, hit.distance, 1e-9);
    }

    // This used to miss. The ray crosses the triangle's plane at the antipode
    // first, and only that first crossing was tested.
    @Test
    void triangleIsVisibleTheLongWayRound() {
        Intersection hit = triangleAtPole().intersect(new Ray(ORIGIN, AWAY));

        assertNotNull(hit);
        assertEquals(LONG_WAY, hit.distance, 1e-9);
    }

    @Test
    void rayPastTheTriangleMisses() {
        Vec4 sideways = new Vec4(0, 1, 0, 0);

        assertNull(triangleAtPole().intersect(new Ray(ORIGIN, sideways)));
    }
}
