package sim.math;

public class Vector3 {
    public final double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 scale(double k) {
        return new Vector3(k * x, k * y, k * z);
    }

    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 normalize() {
        double mag = this.mag();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalise zero vector");
        }
        return new Vector3(x / mag, y / mag, z / mag);
    }


    public Vector3 cross(Vector3 v) {
        return new Vector3(y*v.z-z*v.y, z*v.x-x*v.z, x*v.y-y*v.x);
    }
}