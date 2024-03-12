package org.example;

import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Dominik Grzelak
 */
@ExtendWith(InjectionExtension.class)
public abstract class AbstractTestSupport {

    public InputStream getResourceAsStream(String filename) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }

    public URI getResourceAsURI(String filename) {
        try {
            return AbstractTestSupport.class.getClassLoader().getResource(filename).toURI();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
