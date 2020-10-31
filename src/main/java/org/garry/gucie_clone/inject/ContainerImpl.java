package org.garry.gucie_clone.inject;


import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

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

    static class ParameterInjector<T> {

        final ExternalContext<T> externalContext;
        final InternalFactory<? extends T> factory;

        public ParameterInjector(ExternalContext<T> externalContext,
                                 InternalFactory<? extends T> factory) {
            this.externalContext = externalContext;
            this.factory = factory;
        }

        T inject(Member member, InternalContext context){
            ExternalContext<?> previous = context.getExternalContext();
            context.setExternalContext(externalContext);
            try {
                return factory.create(context);
            }finally {
                context.setExternalContext(previous);
            }
        }
    }

    static class FieldInjector implements Injector {

        final Field field;
        final InternalFactory<?> factory;
        final ExternalContext<?> externalContext;

        public FieldInjector(ContainerImpl container, Field field, String name)
                throws MissingDependencyException {
            this.field = field;
            field.setAccessible(true);

            Key<?> key = Key.newInstance(field.getType(), name);
            factory = container.getFactory(key);
            if (factory == null){
                throw new MissingDependencyException(
                        "No mapping found for dependency " + key + "in " + field + ".");
            }
            this.externalContext = ExternalContext.newInstance(field, key, container);
        }

        @Override
        public void inject(InternalContext context, Object o) {
            ExternalContext<Object> previous = context.getExternalContext();
            context.setExternalContext(externalContext);
            try {
                field.set(o, factory.create(context));
            }catch (IllegalAccessException e){
                throw new AssertionError(e);
            }finally {
                context.setExternalContext(previous);
            }
        }
    }

    <T> InternalFactory<? extends T> getFactory(Key<T> key) {
        return (InternalFactory<T>) factories.get(key);
    }

    static class MethodInject implements Injector {

        final Method method;
        final ParameterInjector<?>[] parameterInjectors;

        public MethodInject(ContainerImpl container, Method method, String name) throws MissingDependencyException {
            this.method = method;
            method.setAccessible(true);

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0){
                throw new DependencyException(
                        method + "has no parameters to inject.");
            }
            parameterInjectors = container.getParametersInjectors(
                    method, method.getParameterAnnotations(), parameterTypes, name);
        }

        @Override
        public void inject(InternalContext context, Object o) {
            try {
                method.invoke(o, getParameters(method, context, parameterInjectors));
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    private static Object[] getParameters(Member member, InternalContext context,
                                          ParameterInjector[] parameterInjectors){
        if (parameterInjectors == null){
            return null;
        }

        Object[] parameters = new Object[parameterInjectors.length];
        for (int i = 0; i < parameters.length; i++){
            parameters[i] = parameterInjectors[i].inject(member, context);
        }
        return parameters;
    }



    <M extends AccessibleObject & Member> ParameterInjector<?> [] getParametersInjectors(
            M member, Annotation[][] annotations, Class[] parameterTypes, String defaultName)
        throws MissingDependencyException {
        List<ParameterInjector<?>> parameterInjectors =
                new ArrayList<>();

        Iterator<Annotation[]> annotationsInjector =
                Arrays.asList(annotations).iterator();
        for (Class<?> parameterType : parameterTypes){
            Inject annotation = findInject(annotationsInjector.next());
            String name = annotation == null ?
                    defaultName : annotation.value();
            Key<?> key = Key.newInstance(parameterType, name);
            parameterInjectors.add(creatrParameterInjector(key, member));
        }
        return toArray(parameterInjectors);
    }

    private ParameterInjector<?>[] toArray(
            List<ParameterInjector<?>> parameterInjections){
        return parameterInjections.toArray(
                new ParameterInjector[parameterInjections.size()]);
    }

    <T> ParameterInjector<T> creatrParameterInjector(
            Key<T> key, Member member) throws MissingDependencyException {
        InternalFactory<? extends T> factory = getFactory(key);
        if (factory == null){
            throw new MissingDependencyException(
                    "No mapping found for dependency " + key + " in " + member + ".");
        }

        ExternalContext<T> externalContext = ExternalContext.newInstance(member, key, this);
        return new ParameterInjector<T>(externalContext,factory);
    }

    /**
     * Finds the {@link Inject} annotation in an array of annotations.
     * @param annotations
     * @return
     */
    Inject findInject(Annotation[] annotations){
        for (Annotation annotation : annotations){
            if (annotation.annotationType() == Inject.class){
                return Inject.class.cast(annotation);
            }
        }
        return null;
    }

    static class ConstructorInjector<T> {

        final Class<T> implementation;
        final List<Injector> injectors;
        final Constructor<T> constructor;
        final ParameterInjector<?>[] parameterInjectors;

        ConstructorInjector(ContainerImpl container, Class<T> implementation){
            this.implementation = implementation;

            constructor = findConstructorIn(implementation);
            constructor.setAccessible(true);

            try {
                Inject inject = constructor.getAnnotation(Inject.class);
                parameterInjectors = inject == null
                        ? null // default constructor
                        : container.getParametersInjectors(
                            constructor,
                            constructor.getParameterAnnotations(),
                            constructor.getParameterTypes(),
                            inject.value()
                          );
            }catch (MissingDependencyException e){
                throw new DependencyException(e);
            }
            injectors = container.injectors.get(implementation);
        }

        private Constructor<T> findConstructorIn(Class<T> implementation){
            Constructor<T> found = null;
            for (Constructor<?> constructor : implementation.getDeclaredConstructors()){
                if (constructor.getAnnotation(Inject.class) != null){
                    if (found != null){
                        throw new DependencyException("More than one constructor annotated" +
                                " with @Inject found in " + implementation + ".");
                    }
                    found = (Constructor<T>) constructor;
                }
            }
            if (found != null){
                return found;
            }

            // If no annotated constructor is found, look for a no-arg constructor instrad
            try {
                return implementation.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
               throw new DependencyException("Could not find a suitable constructor "
               + " in " + implementation.getName() + ".");
            }
        }

        Object construct(InternalContext context, Class<? super T> expectedType) {
            ConstructionContext<T> constructionContext =
                    context.getConstructionContext(this);

            // we have a circular reference between constructors. Return a proxy.
            if (constructionContext.isConstructing()){
                // if we can't proxy this object, can we proxy the other object?
                return constructionContext.createProxy(expectedType);
            }

            // If we're re-entering this factory while injecting fields or methods,
            // return the same instance. This prevents infinite loops
            T t = constructionContext.getCurrentReference();
            if (t != null){
                return t;
            }

            try {
                // First time through...
                constructionContext.startConstructing();
                try {
                    Object[] parameters = getParameters(constructor, context, parameterInjectors);
                    t = constructor.newInstance(parameters);
                    constructionContext.setProxyDelegates(t);
                }finally {
                    constructionContext.finishConstruction();
                }

                // store reference. If an injector re-enters this factory, they'll
                // get the same reference
                constructionContext.setCurrentReference(t);

                // Inject fields and methods.
                for (Injector injector : injectors){
                    injector.inject(context, t);
                }

                return t;
            }catch (InstantiationException e){
                throw new RuntimeException(e);
            }catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }catch (InvocationTargetException e){
                throw new RuntimeException(e);
            }finally {
                constructionContext.removeCurrentReference();
            }
        }
    }

    final Map<Class<?>, List<Injector>> injectors =
            new ReferenceCache

    interface ContextualCallable<T> {
        T call(InternalContext context);
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