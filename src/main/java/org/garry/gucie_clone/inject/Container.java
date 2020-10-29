package org.garry.gucie_clone.inject;

/**
 * Injects dependencies into constructors, methods and fields annotated with {@link Inject}. Immutable
 *
 * <pre>
 *     Container c = ...;
 *     Foo foo = c.inject(Foo.class);
 * </pre>
 */
public interface Container {

    String DEFAULT_NAME = "default";

    /**
     * Injects dependencies into the fields and methods of an existing object
     * @param o
     */
    void inject(Object o);

    <T> T inject(Class<T> implementation);

    <T> T getInstance(Class<T> type, String name);

    <T> T getInstance(Class<T> type);

    /**
     * Sets the scope strategy for the current thread
     * @param scopeStrategy
     */
    void setScopeStrategy(Scope.Strategy scopeStrategy);

    void removeScopeStrategy();
}
