package org.garry.gucie_clone.inject;

import java.lang.reflect.Member;
import java.util.*;
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

    /**
     * Maps an implementation class to a given dependency type and name.
     * Creates instance using the container,recursively injecting dependencies
     *
     * @param type type of dependency
     * @param name name of dependency
     * @param implementation class
     * @param scope scope of injected instances
     * @param <T>
     * @return this builder
     */
    public <T> ContainerBuilder factory(final Class<T> type, final String name,
                                        final Class<? extends T> implementation, final Scope scope){

        // This factory creates new instances of the given implementation.
        // we have to lazy load the constructor because the Container hasn't been created yet
        InternalFactory<T> factory = new InternalFactory<T>() {

            volatile ContainerImpl.ConstructorInjector<? extends T> constructor;

            @Override
            public T create(InternalContext context) {
                if (constructor == null) {
                    this.constructor =
                            context.getContainerImpl().getConstructor(implementation);
                }
                return (T) constructor.construct(context, type);
            }

            @Override
            public String toString() {
                return new LinkedHashMap<String, Object>() {
                    {
                        put("type", type);
                        put("name", name);
                        put("implementation", implementation);
                        put("scope", scope);
                    }
                }.toString();
            }
        };
        return factory(Key.newInstance(type, name), factory, scope);

    }

    /**
     * Maps a dependency. All methods in this class ultimately funnel through here
     * @param key
     * @param factory
     * @param scope
     * @param <T>
     * @return
     */
    private <T> ContainerBuilder factory(final Key<T> key,
                                         InternalFactory<? extends T> factory, Scope scope){
        ensureNotCreated();
        checkKey(key);
        final InternalFactory<? extends T> scopedFactory =
                scope.scopeFactory(key.getType(), key.getName(), factory);
        factories.put(key, scopedFactory);
        if (scope == Scope.SINGLETON) {
            singletonFactories.add(new InternalFactory<T>() {
                @Override
                public T create(InternalContext context) {
                   try {
                       context.setExternalContext(ExternalContext.newInstance(
                               null, key, context.getContainerImpl()));
                       return scopedFactory.create(context);
                   }finally {
                       context.setExternalContext(null);
                   }
                }
            });
        }
        return this;
    }

    /**
     * Ensures a key isn't alredy mapped
     * @param key
     */
    private void checkKey(Key<?> key){
        if (factories.containsKey(key)){
            throw new DependencyException(
                    "Dependency mapping for " + key + " already exists.");
        }
    }

    /**
     * Maps a constant value to the given type and name.
     * @param type
     * @param name
     * @param value
     * @param <T>
     * @return
     */
    private <T> ContainerBuilder constant(final Class<T> type, final String name,
                                          final T value){
        InternalFactory<T> factory = new InternalFactory<T>() {
            @Override
            public T create(InternalContext context) {
                return value;
            }

            @Override
            public String toString() {
               return new LinkedHashMap<String, Object>(){
                   {
                       put("type", type);
                       put("name", name);
                       put("value", value);
                   }
               }.toString();
            }
        };

        return factory(Key.newInstance(type, name),factory, Scope.DEFAULT);

    }

    /**
     * Maps a constant value to the given name
     * @param name
     * @param value
     * @return
     */
    public ContainerBuilder constant(String name, String value){
        return constant(String.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, int value) {
        return constant(int.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, long value) {
        return constant(long.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, boolean value) {
        return constant(boolean.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, double value) {
        return constant(double.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, float value) {
        return constant(float.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, short value) {
        return constant(short.class, name, value);
    }

    /**
     * Maps a constant value to the given name.
     */
    public ContainerBuilder constant(String name, char value) {
        return constant(char.class, name, value);
    }

    /**
     * Maps a class to the given name.
     */
    public ContainerBuilder constant(String name, Class value) {
        return constant(Class.class, name, value);
    }

    /**
     * Maps an enum to the given name.
     */
    public <E extends Enum<E>> ContainerBuilder constant(String name, E value) {
        return constant(value.getDeclaringClass(), name, value);
    }

    /**
     * Upon creation, the {@link Container} will inject static fields and methods
     * into the given classed
     * @param types for which static members will be injected
     * @return
     */
    public ContainerBuilder injectStatics(Class<?>... types){
        staticInjections.addAll(Arrays.asList(types));
        return this;
    }
}
