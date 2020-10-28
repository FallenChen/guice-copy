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



}
