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
}
