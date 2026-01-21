package physics;

import manifold.Manifold;
import manifold.Vec3;

/**
 * A free particle moving along geodesics.
 * 
 * In curved space, a free particle (no forces) follows a geodesic.
 * This is implemented using the manifold's exponential map and
 * parallel transport.
 * 
 * @param <P> The manifold's point type
 */
public class Particle<P> {

    private final Manifold<P> manifold;
    private P position;
    private Vec3 velocity; // Tangent vector at current position

    // Visualization
    private final Vec3 color;
    private final double radius;

    public Particle(Manifold<P> manifold, P initialPosition, Vec3 initialVelocity) {
        this(manifold, initialPosition, initialVelocity, new Vec3(1, 0.3, 0.3), 0.05);
    }

    public Particle(Manifold<P> manifold, P initialPosition, Vec3 initialVelocity,
            Vec3 color, double radius) {
        this.manifold = manifold;
        this.position = initialPosition;
        this.velocity = initialVelocity;
        this.color = color;
        this.radius = radius;
    }

    /**
     * Update the particle's position by moving along the geodesic.
     * 
     * @param dt Time step
     */
    public void update(double dt) {
        // Calculate displacement vector
        Vec3 displacement = velocity.scale(dt);

        // Move along geodesic
        P newPosition = manifold.exp(position, displacement);

        // Parallel transport the velocity to the new position
        velocity = manifold.parallelTransport(velocity, position, newPosition);

        position = newPosition;
    }

    /**
     * Get the particle's position in R³ visualization coordinates.
     */
    public Vec3 getPositionR3() {
        return manifold.projectToR3(position);
    }

    /**
     * Get the velocity direction in R³ (for visualization).
     */
    public Vec3 getVelocityR3() {
        return manifold.tangentToR3(position, velocity);
    }

    public P getPosition() {
        return position;
    }

    public void setPosition(P position) {
        this.position = position;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    public double getSpeed() {
        return Math.sqrt(manifold.innerProduct(position, velocity, velocity));
    }

    public Vec3 getColor() {
        return color;
    }

    public double getRadius() {
        return radius;
    }
}
