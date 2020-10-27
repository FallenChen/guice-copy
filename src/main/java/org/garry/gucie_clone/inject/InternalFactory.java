package org.garry.gucie_clone.inject;

/**
 * Creates objects which will be injected
 * @param <T>
 */
public interface InternalFactory<T> {

    /**
     * Creates an object to be injected
     * @param context of this injection
     * @return instance to be injected
     */
    T create(InternalContext context);
}
