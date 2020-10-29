package org.garry.gucie_clone.inject;


import java.util.concurrent.Callable;

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
                    synchronized (context.getContainer()){
                        if (instance == null){
                            instance = factory.create(context);
                        }
                        return instance;
                    }
                }
                public String toString(){
                    return factory.toString();
                }
            };
        }
    },

    /**
     * One instance per thread
     *
     * if a thread local object strongly reference its {@link Container},
     * neither the {@code Container} nor the object will be
     * eligible for garbage collection, i.e. memory leak
     */

    THREAD {
        <T> InternalFactory<? extends T> scopeFactory(Class<T> type, String name,
                final InternalFactory<? extends T> factory){
            return new InternalFactory<T>(){
                final ThreadLocal<T> threadLocal = new ThreadLocal<T>();
                public T create(final InternalContext context){
                    T t = threadLocal.get();
                    if (t == null){
                        t = factory.create(context);
                        threadLocal.set(t);
                    }
                    return t;
                }

                public String toString(){
                    return factory.toString();
                }
            };
        }
    },

    /**
     * One instance per request
     */
    REQUEST{
        <T> InternalFactory<? extends T> scopeFactory(final Class<T> type,
                                                      final String name,final InternalFactory<? extends T> factory){
            return new InternalFactory<T>() {
                @Override
                public T create(InternalContext context) {
                    Strategy strategy = context.getScopeStrategy();
                    try {
                        return strategy.findInRequest(
                                type, name, toCallable(context, factory));
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }

                public String toString(){
                    return factory.toString();
                }
            };
        }
    },

    /**
     * One instance per session.
     */
    SESSION {
        <T> InternalFactory<? extends T> scopeFactory(final Class<T> type,
                                                      final String name, final InternalFactory<? extends T> factory) {
            return new InternalFactory<T>() {
                public T create(InternalContext context) {
                    Strategy strategy = context.getScopeStrategy();
                    try {
                        return strategy.findInSession(
                                type, name, toCallable(context, factory));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                public String toString() {
                    return factory.toString();
                }
            };
        }
    },

    /**
     * One instance per wizard.
     */
    WIZARD {
        <T> InternalFactory<? extends T> scopeFactory(final Class<T> type,
                                                      final String name, final InternalFactory<? extends T> factory) {
            return new InternalFactory<T>() {
                public T create(InternalContext context) {
                    Strategy strategy = context.getScopeStrategy();
                    try {
                        return strategy.findInWizard(
                                type, name, toCallable(context, factory));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                public String toString() {
                    return factory.toString();
                }
            };
        }
    };

    <T> Callable<? extends T> toCallable(final InternalContext context,
                                         final InternalFactory<? extends T> factory){
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                return factory.create(context);
            }
        };
    }

    /**
     * Wraps factory with scoping logic
     */
    abstract <T> InternalFactory<? extends T> scopeFactory(
            Class<T> type, String name, InternalFactory<? extends T> factory);


    /**
     * Pluggable scoping strategy. Enables users to provide custom
     * implementations of request, session, and wizard scopes.Implement and
     * pass to {@link Container#setScopeStrategy(Strategy)}
     */
    public interface Strategy{

        /**
         * Finds an object for the given type and name in the request scope.
         * Creates a new object if necessary using the given factory
         * @param type
         * @param name
         * @param factory
         * @param <T>
         * @return
         * @throws Exception
         */
        <T> T findInRequest(Class<T> type, String name,
                            Callable<? extends T> factory)throws Exception;

        <T> T findInSession(Class<T> type, String name,
                            Callable<? extends T> factory)throws Exception;

        <T> T findInWizard(Class<T> type, String name,
                            Callable<? extends T> factory)throws Exception;

    }

}
