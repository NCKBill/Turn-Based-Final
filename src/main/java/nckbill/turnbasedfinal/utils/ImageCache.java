package nckbill.turnbasedfinal.utils;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Find images inside resources folder
 * Parse URL and return Image of desired assets
 */
public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image getImage(String path) {
        if (!cache.containsKey(path)) {
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