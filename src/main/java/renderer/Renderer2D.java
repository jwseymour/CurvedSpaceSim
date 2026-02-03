package renderer;

import java.awt.image.BufferedImage;
import java.util.List;
import scene.objects.SceneObject2D;
import geometry.Space2D;
import manifold.Vec3;
import rays.Ray2D;

/**
 * Renders the 2D world.
 * Currently supports:
 * - First Person (Panoramic) Ray Casting
 */
public class Renderer2D {

    /**
     * @param renderMode 0 = 2.5D (Walls), 1 = 1D Strip (Strict S2 view)
     */
    public BufferedImage render(Space2D space, List<SceneObject2D> objects, Camera2D camera, int width, int height,
            int renderMode) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Ceiling/Sky color
        int skyColor = 0x222244; // Dark blue-ish gray
        double wallHeightScale = height; // Adjust for FOV/Aspect

        // Scan columns (Angle)
        for (int x = 0; x < width; x++) {
            // Map x to [-1, 1] relative to center
            double ndc = (x + 0.5) / width * 2.0 - 1.0;
            Ray2D ray = camera.getRay(ndc, space);

            // Cast ray logic
            double minDist = Double.POSITIVE_INFINITY;
            SceneObject2D closest = null;

            for (SceneObject2D obj : objects) {
                double t = obj.intersect(ray, space);
                if (t > 0 && t < minDist) {
                    minDist = t;
                    closest = obj;
                }
            }

            if (renderMode == 1) {
                // 1D Mode: The entire column is the color of the intersection.
                // We stretch it vertically just so the human user can see the 1D line.
                int colColor;
                if (closest != null) {
                    // Fog
                    double fogAmount = Math.exp(-minDist * 0.1);
                    Vec3 finalCol = closest.getColor().scale(fogAmount);
                    colColor = vec3ToInt(finalCol);
                } else {
                    // Sky/Void color
                    colColor = skyColor;
                }

                // Draw vertical strip
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, colColor);
                }
                continue;
            }

            // --- 2.5D Mode (Walls) ---

            // Calculate wall height
            // Perspective projection: h ~ 1/z
            // Avoid divide by zero
            double dist = Math.max(0.1, minDist);
            int wallHeight = (int) (wallHeightScale / dist);

            // Center the wall vertically
            int drawStart = (height - wallHeight) / 2;
            int drawEnd = drawStart + wallHeight;

            // Clamp
            int wallStart = Math.max(0, drawStart);
            int wallEnd = Math.min(height, drawEnd);

            // Draw Column
            for (int y = 0; y < height; y++) {
                if (y < wallStart) {
                    // Ceiling / Sky
                    image.setRGB(x, y, skyColor);
                } else if (y >= wallEnd) {
                    // Floor
                    // Calculate distance to floor point
                    // y_screen relative to horizon (height/2)
                    // dy = y - height/2
                    // dist_floor = camera_height / dy * const
                    double dy = (y - height / 2.0) / (height / 2.0); // 0 to 1
                    double floorDist = 1.0 / Math.max(0.001, dy);

                    // Get floor position
                    Ray2D floorRay = space.advance(ray, floorDist);
                    Vec3 p = floorRay.p;

                    int colorVal = getFloorColor(p);

                    // Distance fog for floor
                    double fog = Math.exp(-floorDist * 0.2);
                    int r = (int) (((colorVal >> 16) & 0xFF) * fog);
                    int g = (int) (((colorVal >> 8) & 0xFF) * fog);
                    int b = (int) ((colorVal & 0xFF) * fog);

                    image.setRGB(x, y, (r << 16) | (g << 8) | b);

                } else {
                    // Wall
                    if (closest != null) {
                        Vec3 col = closest.getColor();
                        double fogAmount = Math.exp(-minDist * 0.1);
                        Vec3 finalCol = col.scale(fogAmount);
                        image.setRGB(x, y, vec3ToInt(finalCol));
                    } else {
                        // Should not happen given logic, but just in case
                        image.setRGB(x, y, 0);
                    }
                }
            }
        }

        return image;
    }

    private int getFloorColor(Vec3 p) {
        // Floor colors (gamma corrected-ish)
        int floorBase = 0x333333; // Dark Gray
        int floorCheck = 0x444444; // Slightly lighter
        int roadColor = 0x888888; // Light Gray Road
        int roadLine = 0xFFFFFF; // White

        // --- Road Logic ---
        // A "Road" along the Equator (z=0)
        // Width of road: approx 1.5 units? (Assuming R=10 usually)
        double roadWidth = 1.5;
        double lineWidth = 0.1;

        // Check distance from Equator (z=0 implies distance from poles is roughly
        // related to z)
        // For S2, z coordinate is literally distance from XY plane.
        // If |z| < width, we are on the road.
        if (Math.abs(p.z) < roadWidth) {
            // On the road
            // Center line? Dash it?
            // Dash based on x or y (longitude)
            // atan2(y, x) is longitude
            double lon = Math.atan2(p.y, p.x);
            if (Math.abs(p.z) < lineWidth && Math.sin(lon * 20) > 0) {
                return roadLine;
            }
            return roadColor;
        }

        // --- Grid Pattern ---
        // Coordinate independent grid
        // Scale factor for frequency
        double scale = 1.0;
        boolean check = (Math.floor(p.x * scale) + Math.floor(p.y * scale) + Math.floor(p.z * scale)) % 2 == 0;
        return check ? floorCheck : floorBase;
    }

    private int vec3ToInt(Vec3 v) {
        int r = (int) (Math.max(0, Math.min(1, v.x)) * 255);
        int g = (int) (Math.max(0, Math.min(1, v.y)) * 255);
        int b = (int) (Math.max(0, Math.min(1, v.z)) * 255);
        return (r << 16) | (g << 8) | b;
    }
}
