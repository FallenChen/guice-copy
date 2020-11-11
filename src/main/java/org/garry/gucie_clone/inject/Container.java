package org.garry.gucie_clone.inject;

/**
 * Injects dependencies into constructors, methods and fields annotated with {@link Inject}. Immutable
 *
 * When injecting a method or constructor, you can additionally annotate
 * its parameters with {@link Inject} and specify a dependency name. When a
 * parameter has no annotation, the container uses the name from the method or constructor's {@link Inject}
 * annotation respectively
 *
 * IOC 容器
 *
 * <pre>
 *     Container c = ...;
 *     Foo foo = c.inject(Foo.class);
 * </pre>
 */
public interface Container {

    /**
     * Default dependency name
     */
    String DEFAULT_NAME = "default";

    /**
     * Injects dependencies into the fields and methods of an existing object
     * @param o
     */
    void inject(Object o);

    /**
     * Creates and injects a new instance of type {@code implementation}
     * @param implementation
     * @param <T>
     * @return
     */
    <T> T inject(Class<T> implementation);

    /**
     * Gets an instance of the given dependency which was declared in
     * {@link ContainerBuilder}
     * @param type
     * @param name
     * @param <T>
     * @return
     */
    <T> T getInstance(Class<T> type, String name);

    /**
     * Convenience method,Equivalent to {@code getInstance(type,DEFAULT_NAME)}
     * @param type
     * @param <T>
     * @return
     */
    <T> T getInstance(Class<T> type);

    /**
     * Sets the scope strategy for the current thread
     * @param scopeStrategy
     */
    void setScopeStrategy(Scope.Strategy scopeStrategy);

    /**
     * Removes the scope strategy for the current thread
     */
    void removeScopeStrategy();
}
