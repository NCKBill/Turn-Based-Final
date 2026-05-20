package nckbill.turnbasedfinal;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image getImage(String path) {
        if (!cache.containsKey(path)) {
            // Find the resource inside the src/main/resources folder
            URL imageUrl = ImageCache.class.getResource(path);

            if (imageUrl == null) {
                System.err.println("CRITICAL: Could not find image at " + path);
                return null;
            }

            cache.put(path, new Image(imageUrl.toExternalForm()));
        }
        return cache.get(path);
    }
}