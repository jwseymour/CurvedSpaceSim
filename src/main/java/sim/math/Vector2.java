package sim.math;

public class Vector2 {
    public final double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    public Vector2 subtract(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    public Vector2 scale(double k) {
        return new Vector2(k * x, k * y);
    }

    public double dot(Vector2 v) {
        return x * v.x + y * v.y;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize() {
        double mag = this.mag();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalise zero vector");
        }
        return new Vector2(x / mag, y / mag);
    }
}
