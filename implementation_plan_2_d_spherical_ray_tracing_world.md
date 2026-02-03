# Implementation Plan — 2D Spherical Ray Tracing World

This document is a **step-by-step implementation plan** for adding a **2D spherical (S²) ray-traced world** to an existing project that already supports **3D Euclidean ray tracing**.

The plan is written with **clarity, extensibility, and presentation/demo use** as first-class goals. It is intended to be copyable, shareable, and executable as a development roadmap.

---

## 0. Guiding principles

Before any code is written, the following principles should guide *every* design decision:

1. **Intrinsic-first thinking**  
   The world is spherical *intrinsically*. The 3D embedding exists only to make the math simple.

2. **Clear separation of concerns**  
   Geometry ≠ Rays ≠ Objects ≠ Rendering ≠ UI.

3. **Pedagogy-aware implementation**  
   The system should make spherical geometry *obvious*, not just correct.

4. **Dimensional scalability**  
   Every abstraction should lift naturally from:
   - 2D Euclidean → 2D Spherical → 3D Euclidean → 3D Spherical

---

## 1. Target outcome for Phase 1

At the end of this phase, the project should support:

- A **2D Euclidean world** (baseline)
- A **2D Spherical world (S²)** with:
  - geodesic ray tracing
  - visible curvature effects
  - UI toggles for live demonstration

The 3D spherical world (S³) is *explicitly out of scope* for this phase, but the architecture must make it a direct extension.

---

## 2. High-level architecture

Introduce an explicit `space` abstraction that owns all geometric meaning.

```
project/
  geometry/
    euclidean/
      space2d
      space3d
    spherical/
      space2d   ← IMPLEMENT
      space3d   ← STUB

  rays/
    ray2d
    ray3d

  scene/
    objects/
    materials/

  renderer/
    renderer2d
    renderer3d

  ui/
    controls
    overlays
```

No rendering code should assume Euclidean geometry.

---

## 3. Geometry layer (core math)

### 3.1 Define a `Space2D` interface

Every 2D space must define:

- Point representation
- Tangent vector representation
- Distance function
- Ray initialization
- Ray advancement
- Angle measurement

Example responsibilities (language-agnostic):

- `project_to_tangent(p, v)`
- `distance(p, q)`
- `make_ray(origin, direction)`
- `advance_ray(ray, t)`

---

### 3.2 Implement `EuclideanSpace2D`

This already exists implicitly or explicitly.

Ensure it conforms to the new `Space2D` interface so it can be swapped live in the UI.

---

### 3.3 Implement `SphericalSpace2D` (S² embedded in ℝ³)

#### Representation

- Point: `vec3 p` with `|p| = R`
- Tangent vector: `vec3 v` with `p · v = 0`

#### Distance

```
d(p, q) = arccos( dot(p, q) / R^2 ) * R
```

#### Ray / geodesic equation

For arc-length parameter `t`:

```
x(t) = p * cos(t/R) + v * sin(t/R)
```

#### Tangent projection

Any vector `w` must be projected as:

```
w_tangent = w - dot(w, p) * p / R^2
```

This function should be centralized and reused everywhere.

---

## 4. Ray layer

### 4.1 Define `Ray2D`

A ray is space-dependent.

For spherical space, it stores:
- origin point `p ∈ S²`
- tangent direction `v ∈ T_p S²`

### 4.2 Ray advancement

- Rays advance by **arc length**, not linear distance
- Rays must never drift off the manifold

All ray logic should live here, not in the renderer.

---

## 5. Scene objects (2D spherical primitives)

### 5.1 Required primitives

Implement **intrinsic** spherical objects only:

- Geodesic circle  
  Defined as all points `x` such that:
  ```
  distance(x, center) = r
  ```

- Point marker
- Great-circle line (optional, for demos)

Avoid Euclidean concepts like planes or infinite lines.

---

### 5.2 Ray–object intersection

For a geodesic circle:

Solve:
```
dot( x(t), c ) = cos(r / R) * R^2
```

This reduces to a trigonometric equation with closed-form solutions.

No ray marching in this phase.

---

## 6. Rendering layer (2D output)

### 6.1 Renderer responsibilities

- Cast rays per pixel
- Accumulate nearest intersection
- Shade based on spherical distance and angle

The renderer must not know whether space is Euclidean or spherical.

---

### 6.2 Visualization modes (important)

Add non-photorealistic modes for explanation:

- Distance heatmap
- Geodesic path overlay
- Ray fan visualization
- Antipodal point markers

These modes are *features*, not debug hacks.

---

## 7. Camera model (2D spherical)

### 7.1 Camera state

- Position `p ∈ S²`
- Forward direction `f ∈ T_p S²`

### 7.2 Movement

- Forward motion = geodesic motion
- Rotation = tangent-space rotation followed by reprojection

Camera motion must obey the same geometry as rays.

---

## 8. UI and presentation layer

This is a **first-class requirement**, not polish.

### 8.1 Space selector

- Euclidean 2D
- Spherical 2D
- (Disabled) Spherical 3D

Switching must be instantaneous.

---

### 8.2 Demo toggles

- Show ray paths
- Show geodesics
- Show curvature effects
- Show closed-loop paths

---

### 8.3 Parameter controls

- Sphere radius (curvature strength)
- Field of view
- Camera speed

Changing curvature should visibly morph the world.

---

## 9. Required demonstration scenarios

At least three of the following must be implemented:

1. **Straight-line looping**  
   Walk forward → return to start

2. **No parallel lines**  
   Emit multiple rays → observe convergence

3. **Triangle angle excess**  
   Render triangle + angle sum

4. **Back-of-the-head visibility**  
   Objects behind camera reappear

These should be toggleable and presentable live.

---

## 10. Validation checklist

Before moving on to S³, confirm:

- Rays stay on the sphere indefinitely
- Distance is symmetric and metric-consistent
- Euclidean and spherical modes share renderer code
- Visual demos behave as predicted mathematically

If any of these fail, do not proceed upward in dimension.

---

## 11. Explicit non-goals (Phase 1)

- Hyperbolic geometry
- Numerical geodesic integration
- Realistic lighting
- Performance optimization

Correctness and explainability come first.

---

## 12. Forward compatibility note

If implemented as specified:

- S³ becomes a matter of:
  - replacing `vec3` with `vec4`
  - increasing tangent dimension
  - reusing geodesic math

No architectural rewrite should be necessary.

---

**End of implementation plan**

