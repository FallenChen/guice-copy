package org.garry.gucie_clone.inject;

import javax.jws.Oneway;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal context. Used to coordinate injections and support circular
 * dependencies
 */
class InternalContext {

    // 一定要实现类了，因为这是粘合的起点
    final ContainerImpl container;

    final Map<Object, ConstructionContext<?>> constructionContexts =
            new HashMap<>();

    Scope.Strategy scopeStrategy;

    ExternalContext<?> externalContext;

    InternalContext(ContainerImpl container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

    ContainerImpl getContainerImpl(){
        return container;
    }

    Scope.Strategy getScopeStrategy() {
        if (scopeStrategy == null) {
            scopeStrategy = container.localScopeStrategy.get();

            if (scopeStrategy == null) {
                throw new IllegalStateException("Scope strategy not set. "
                        + "Please call Container.setScopeStrategy().");
            }
        }

        return scopeStrategy;
    }
}
