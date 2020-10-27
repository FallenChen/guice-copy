package org.garry.gucie_clone.inject;

/**
 * Scope of an injected objects
 */
public enum Scope {

    /**
     * One instance per injection
     */
    DEFAULT {
        <T> InternalFactory<? extends T> scopeFactory(Class<T> type, String name,
                                                      InternalFactory<? extends T> factory){
            return factory;
        }
    },

    /**
     * One instance per container
     */

    SINGLETON {
        <T> InternalFactory<? extends T> scopeFactory(Class<T> type, String name,
                                                      final InternalFactory<? extends T> factory){
            return new InternalFactory<T>() {
                T instance;

                @Override
                public T create(InternalContext context) {
                    synchronized (context)
                    return null;
                }
            }
        }
    }

}
