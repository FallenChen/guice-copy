package org.garry.gucie_clone.inject;


import java.util.Map;

class ContainerImpl implements Container {

    final Map<Key<?>, InternalFactory<?>> factories;

    ContainerImpl(Map<Key<?>, InternalFactory<?>> factories){
        this.factories = factories;
    }


    interface Injector {
        void inject(InternalContext context, Object o);
    }

    static class MissingDependencyException extends Exception {
        MissingDependencyException(String message) {
            super(message);
        }
    }



    @Override
    public void inject(Object o) {

    }

    @Override
    public <T> T inject(Class<T> implementation) {
        return null;
    }

    @Override
    public <T> T getInstance(Class<T> type, String name) {
        return null;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return null;
    }
}
