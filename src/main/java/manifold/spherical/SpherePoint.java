package manifold.spherical;

import manifold.Vec4;

/**
 * A point on the 3-sphere (S³).
 * 
 * Points satisfy: x² + y² + z² + w² = R² (embedded in R⁴)
 * 
 * Unlike the hyperboloid which uses Minkowski geometry, S³ uses
 * standard Euclidean geometry in R⁴.
 */
public class SpherePoint {

    public final Vec4 coords;

    /**
     * Create a sphere point from R⁴ coordinates.
     * Assumes the point already lies on S³.
     */
    public SpherePoint(Vec4 coords) {
        this.coords = coords;
    }

    /**
     * Create a sphere point, normalizing to the sphere surface.
     */
    public static SpherePoint normalize(Vec4 v, double R) {
        double norm = v.norm();
        if (norm < 1e-12) {
            throw new IllegalArgumentException("Cannot normalize zero vector to sphere");
        }
        return new SpherePoint(v.scale(R / norm));
    }

    /**
     * Create the "north pole" of S³: (R, 0, 0, 0).
     */
    public static SpherePoint origin(double R) {
        return new SpherePoint(new Vec4(R, 0, 0, 0));
    }

    /**
     * Create a point using "spherical" coordinates.
     * Uses the first coordinate as the "radial" direction (like latitude).
     */
    public static SpherePoint fromSpherical(double R, double chi, double theta, double phi) {
        double sinChi = Math.sin(chi);
        return new SpherePoint(new Vec4(
                R * Math.cos(chi),
                R * sinChi * Math.sin(theta) * Math.cos(phi),
                R * sinChi * Math.sin(theta) * Math.sin(phi),
                R * sinChi * Math.cos(theta)));
    }

    public double w() {
        return coords.t;
    } // Using t as w for clarity

    public double x() {
        return coords.x;
    }

    public double y() {
        return coords.y;
    }

    public double z() {
        return coords.z;
    }

    @Override
    public String toString() {
        return "S³" + coords.toString();
    }
}
