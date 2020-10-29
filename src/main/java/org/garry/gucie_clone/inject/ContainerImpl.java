package org.garry.gucie_clone.inject;


import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.List;
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


    <M extends Member & AnnotatedElement> void addInjectorsForMembers(
            List<M> members, boolean statics, List<Injector> injectors,
            InjectorFactory<M> injectorFactory){
        for (M member: members){
            if (isStatic(member) == statics){
                Inject inject = member.getAnnotation(Inject.class);
                if (inject != null){
                    try {
                        injectors.add(injectorFactory.create(this, member,inject.value()));
                    }catch (MissingDependencyException e){
                        if (inject.required()){
                            throw new DependencyException(e);
                        }
                    }
                }
            }
        }
    }

    private boolean isStatic(Member member){
        return Modifier.isStatic(member.getModifiers());
    }

    interface InjectorFactory<M extends Member & AnnotatedElement> {
        Injector create(ContainerImpl container, M member, String name) throws MissingDependencyException;
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
