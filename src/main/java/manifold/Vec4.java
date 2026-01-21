package manifold;

/**
 * 4D vector used for hyperboloid model (Minkowski space) and S³ embedding.
 * For Minkowski: component 0 is the timelike coordinate.
 */
public class Vec4 {
    public final double t, x, y, z;

    public Vec4(double t, double x, double y, double z) {
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec4 add(Vec4 v) {
        return new Vec4(t + v.t, x + v.x, y + v.y, z + v.z);
    }

    public Vec4 sub(Vec4 v) {
        return new Vec4(t - v.t, x - v.x, y - v.y, z - v.z);
    }

    public Vec4 scale(double s) {
        return new Vec4(t * s, x * s, y * s, z * s);
    }

    /**
     * Euclidean dot product (for S³ in R⁴).
     */
    public double dot(Vec4 v) {
        return t * v.t + x * v.x + y * v.y + z * v.z;
    }

    /**
     * Minkowski inner product: -t² + x² + y² + z²
     * Used for H³ hyperboloid model.
     */
    public double minkowski(Vec4 v) {
        return -t * v.t + x * v.x + y * v.y + z * v.z;
    }

    /**
     * Euclidean norm squared.
     */
    public double normSq() {
        return dot(this);
    }

    /**
     * Euclidean norm.
     */
    public double norm() {
        return Math.sqrt(normSq());
    }

    /**
     * Minkowski "norm" squared (can be negative for timelike vectors).
     */
    public double minkowskiNormSq() {
        return minkowski(this);
    }

    /**
     * Normalize to unit Euclidean length.
     */
    public Vec4 normalize() {
        double n = norm();
        if (n < 1e-12)
            return this;
        return scale(1.0 / n);
    }

    /**
     * Spatial part only (x, y, z).
     */
    public Vec3 spatial() {
        return new Vec3(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vec4(%.4f, %.4f, %.4f, %.4f)", t, x, y, z);
    }
}
