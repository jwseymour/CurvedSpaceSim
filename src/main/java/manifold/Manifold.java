package manifold;

/**
 * Abstract interface for a Riemannian manifold.
 * 
 * All manifolds share this common interface, allowing the ray tracer
 * and physics simulation to work with any geometry.
 * 
 * Key operations:
 * - exp(p, v): Walk from point p in direction v for distance |v| (exponential
 * map)
 * - log(p, q): Get the tangent vector at p pointing toward q (logarithm map)
 * - distance(p, q): Geodesic distance between two points
 * - parallelTransport(v, from, to): Move a vector along the geodesic
 * 
 * @param <P> The point type for this manifold
 */
public interface Manifold<P> {

    /**
     * Exponential map: starting at point p, walk in tangent direction v
     * for distance |v|. Returns the resulting point.
     * 
     * This traces a geodesic from p with initial velocity v.
     */
    P exp(P p, Vec3 v);

    /**
     * Logarithm map: returns the tangent vector at p that points toward q,
     * with magnitude equal to the geodesic distance from p to q.
     * 
     * Satisfies: exp(p, log(p, q)) = q
     */
    Vec3 log(P p, P q);

    /**
     * Geodesic distance between two points.
     */
    double distance(P p, P q);

    /**
     * Parallel transport a tangent vector from point 'from' to point 'to'
     * along the geodesic connecting them.
     * 
     * This preserves the vector's length and its angle relative to the geodesic.
     */
    Vec3 parallelTransport(Vec3 v, P from, P to);

    /**
     * Inner product of two tangent vectors at point p.
     * For constant-curvature spaces, this is often just the Euclidean dot product
     * on the tangent space representation.
     */
    double innerProduct(P p, Vec3 v1, Vec3 v2);

    /**
     * Returns the (constant) sectional curvature of this manifold.
     * K > 0: spherical geometry
     * K = 0: Euclidean geometry
     * K < 0: hyperbolic geometry
     */
    double curvature();

    /**
     * Project a tangent vector at point p to a 3D position for rendering.
     * This converts from the manifold's internal representation to Euclidean R³.
     * 
     * For H³ with hyperboloid model, this projects to the Poincaré ball.
     * For S³, this uses stereographic projection.
     * For E³, this is identity.
     */
    Vec3 projectToR3(P p);

    /**
     * Get a point from R³ coordinates (inverse of projectToR3).
     * Used for setting up initial positions.
     */
    P pointFromR3(Vec3 v);

    /**
     * Get the tangent vector representation in R³ at a given point.
     * Used for rendering velocity vectors, camera directions, etc.
     */
    Vec3 tangentToR3(P p, Vec3 v);
}
