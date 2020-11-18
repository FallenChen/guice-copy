package org.garry.gucie_clone.inject;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Builds a dependency injection {@link Container}. The combination of
 * dependency type and name uniquely identifies a dependency mapping;
 * you can use the same name for two different types.Not safe for concurrent use
 *
 *
 */
public final class ContainerBuilder {

    final Map<Key<?>, InternalFactory<?>> factories =
            new HashMap<>();

    final List<InternalFactory<?>> singletonFactories =
            new ArrayList<>();

    final List<Class<?>> staticInjections = new ArrayList<>();

    boolean created;

    private static final InternalFactory<Container> CONTAINER_FACTORY =
            new InternalFactory<Container>() {
                @Override
                public Container create(InternalContext context) {
                   return context.getContainer();
                }
            };

    private static final InternalFactory<Logger> LOGGER_FACTORY =
            new InternalFactory<Logger>() {
                @Override
                public Logger create(InternalContext context) {
                    Member member = context.getExternalContext().getMember();
                    return member == null ? Logger.getAnonymousLogger()
                            : Logger.getLogger(member.getDeclaringClass().getName());
                }
            };

    /**
     * Constructs a new builder
     */
    public ContainerBuilder() {
        // In the current container as the default Container implementation
        factories.put(Key.newInstance(Container.class, Container.DEFAULT_NAME),
                CONTAINER_FACTORY);

        // Inject the logger for the injected member's declaring class
        factories.put(Key.newInstance(Logger.class, Container.DEFAULT_NAME),
                LOGGER_FACTORY);
    }

    /**
     * Creates a {@link Container} instance. Injects static members for classes
     * which were registered using {@link #injectStatcis(Class...)}.
     *
     * @param loadSingletons if true, the container will load all singletons
     *                       now.If false, the container will lazily load singletons.
     *                       Eager loading is appropriate for production use while lazy loading
     *                       can speed development
     * @return
     */
    public Container create(boolean loadSingletons){
        ensureNotCreated();
        created = true;

        final ContainerImpl container = new ContainerImpl(
                new HashMap<Key<?>, InternalFactory<?>>(factories));
        if (loadSingletons){
            container.callInContext(new ContainerImpl.ContextualCallable<Void>() {
                @Override
                public Void call(InternalContext context) {
                   for (InternalFactory<?> factory : singletonFactories){
                       factory.create(context);
                   }
                   return null;
                }
            });
        }

        container.injectStatics(staticInjections);
        return container;
    }

    /**
     * Currently we only support creating one Container instance per builder.
     * If we want to support creating more than one container per builder.
     * we should move to a "factory factory" model where we create a factory
     * instance per Container. Right now, one factory instance would be shared
     * across all the containers, singletons synchronize on the container when lazy loading etc
     */
    private void ensureNotCreated(){
        if (created){
            throw new IllegalStateException("Container alreday created.");
        }
    }

    /**
     * Convenience method. Equivalent to {@code factory(type, Container.DEFAULT_NAME, implementation}
     * @param type
     * @param implementation
     * @param <T>
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type,
                                        Class<? extends T> implementation){
        return factory(type, Container.DEFAULT_NAME, implementation);
    }

    /**
     * Maps an implementation class to a given dependency type and name.
     * Creates instances using the container, recursively injecting dependencies.
     *
     * Sets scope to value from {@link Scope} annotation on the implementation class.
     * Defaults to {@link Scope#DEFAULT} if no annotation is found
     *
     * @param type
     * @param name
     * @param implementation
     * @param <T>
     * @return
     */
    private <T> ContainerBuilder factory(final Class<T> type, String name,
                                         final Class<? extends T> implementation) {
        Scoped scoped = implementation.getAnnotation(Scoped.class);
        Scope scope = scoped == null ?
                Scope.DEFAULT : scoped.value();
        return factory(type, name, implementation, scope);
    }
}
