package org.garry.gucie_clone.inject;

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

    // scope 策略
    Scope.Strategy scopeStrategy;
    // 对外的快照上下文
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

    <T> ExternalContext<T> getExternalContext(){
        return (ExternalContext)externalContext;
    }

    void setExternalContext(ExternalContext<?> externalContext){
        this.externalContext = externalContext;
    }

    <T> ConstructionContext<T> getConstructionContext(Object key){

        ConstructionContext constructionContext = constructionContexts.get(key);

        if (constructionContext == null){
            constructionContext = new ConstructionContext<T>();
            constructionContexts.put(key,constructionContext);
        }
        return constructionContext;
    }
}
