package org.example.service;

import java.io.InputStream;
import java.net.URL;

/**
 * A helper class to load resources.
 *
 * @author Dominik Grzelak
 */
public class ResourceLoader {
    public static URL getResourceURL(String resourceName) {
        return ResourceLoader.class.getClassLoader().getResource(resourceName);
    }

    public static InputStream getResourceStream(String resourceName) {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(resourceName);
    }
}
