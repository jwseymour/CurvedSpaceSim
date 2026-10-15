package com.curvedspace.app;

import com.curvedspace.tracer.Camera;
import com.curvedspace.tracer.RayTracer;
import com.curvedspace.tracer.Scene;
import com.curvedspace.tracer.SceneFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Headless renderer: writes one PNG per scene, no window required.
 *
 * <pre>./gradlew run -PmainClass=com.curvedspace.app.RenderScenes --args="docs/images"</pre>
 */
public class RenderScenes {
    private static final int WIDTH = 960;
    private static final int HEIGHT = 540;

    public static void main(String[] args) throws IOException {
        File outDir = new File(args.length > 0 ? args[0] : "docs/images");
        if (!outDir.isDirectory() && !outDir.mkdirs()) {
            throw new IOException("Cannot create " + outDir);
        }

        for (int i = 0; i < Main.SCENE_NAMES.length; i++) {
            Scene scene = SceneFactory.create(i);
            Camera camera = SceneFactory.createCamera(i);
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            new RayTracer(scene, camera, WIDTH, HEIGHT).render(image);

            String name = Main.SCENE_NAMES[i].toLowerCase().replace(' ', '-');
            File out = new File(outDir, (i + 1) + "-" + name + ".png");
            javax.imageio.ImageIO.write(image, "png", out);
            System.out.println("Wrote " + out);
        }
    }
}
