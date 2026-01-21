package manifold;

/**
 * 3D vector used for Euclidean space, Poincaré ball model,
 * and tangent vectors in all geometries.
 */
public class Vec3 {
    public final double x, y, z;

    public static final Vec3 ZERO = new Vec3(0, 0, 0);
    public static final Vec3 X = new Vec3(1, 0, 0);
    public static final Vec3 Y = new Vec3(0, 1, 0);
    public static final Vec3 Z = new Vec3(0, 0, 1);

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    public Vec3 sub(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }

    public Vec3 scale(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public Vec3 negate() {
        return new Vec3(-x, -y, -z);
    }

    public double dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x);
    }

    public double normSq() {
        return dot(this);
    }

    public double norm() {
        return Math.sqrt(normSq());
    }

    public Vec3 normalize() {
        double n = norm();
        if (n < 1e-12)
            return this;
        return scale(1.0 / n);
    }

    /**
     * Linear interpolation between this and v.
     */
    public Vec3 lerp(Vec3 v, double t) {
        return this.scale(1 - t).add(v.scale(t));
    }

    /**
     * Reflect this vector about a normal.
     */
    public Vec3 reflect(Vec3 normal) {
        return this.sub(normal.scale(2 * this.dot(normal)));
    }

    /**
     * Component-wise absolute value.
     */
    public Vec3 abs() {
        return new Vec3(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    /**
     * Convert to Vec4 with given t-component.
     */
    public Vec4 toVec4(double t) {
        return new Vec4(t, x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vec3(%.4f, %.4f, %.4f)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3))
            return false;
        Vec3 v = (Vec3) obj;
        double eps = 1e-10;
        return Math.abs(x - v.x) < eps && Math.abs(y - v.y) < eps && Math.abs(z - v.z) < eps;
    }
}
