package org.garry.gucie_clone.inject;

/**
 * Thrown when a dependency is misconfigured.
 */
public class DependencyException extends RuntimeException {

    public DependencyException(String message) {
        super(message);
    }

    public DependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyException(Throwable cause) {
        super(cause);
    }
}
