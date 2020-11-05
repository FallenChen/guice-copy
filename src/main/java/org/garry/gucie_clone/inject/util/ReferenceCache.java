package org.garry.gucie_clone.inject.util;

import java.util.concurrent.*;

/**
 * Extends {@link ReferenceMap} to support lazy loading values by overriding
 * {@link #create(Object)}
 * @param <K>
 * @param <V>
 */
public abstract class ReferenceCache<K, V> extends ReferenceMap<K,V> {

    private static final long serialVersionUID = 0;

    transient ConcurrentMap<Object, Future<V>> futures =
            new ConcurrentHashMap<Object, Future<V>>();

    transient ThreadLocal<Future<V>> localFuture = new ThreadLocal<>();

    public ReferenceCache(ReferenceType keyReferenceType,
                          ReferenceType valueReferenceType){
        super(keyReferenceType,valueReferenceType);
    }

    /**
     * Equivalent to {@code new ReferenceCache(STRONG, STRONG)}
     */
    public ReferenceCache(){
        super(ReferenceType.STRONG, ReferenceType.STRONG);
    }

    /**
     * Override to lazy load values. Use as an alternative to {@link #put(Object, Object)}.
     * Invoked by getter if value isn't already cached.Must not return {@code null}.This
     * method will not be called again until the garbage collector reclaims the returned value.
     */

    protected abstract V create(K key);

    V internalCreate(K key){
        try {
            FutureTask<V> futureTask =
                    new FutureTask<>(new CallableCreate(key));

            // use a reference so we get the same equality semantics
            Object keyReference = referenceKey(key);
            Future<V> future = futures.putIfAbsent(keyReference, futureTask);
            if (future == null){
                // winning thread
                try {
                    if (localFuture.get() != null){
                        throw new IllegalStateException(
                                "Nested creations within the same cache are not allowed.");
                    }
                    localFuture.set(futureTask);
                    futureTask.run();
                    V value = futureTask.get();
                    putStrategy().execute(this,
                            keyReference, referenceValue(keyReference, value));
                    return value;
                }finally {
                    localFuture.remove();
                    futures.remove(keyReference);
                }
            }else {
                // wait for winning thread
                return future.get();
            }
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }catch (ExecutionException e){
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException){
                throw (RuntimeException)cause;
            }else if (cause instanceof Error){
                throw (Error)cause;
            }
            throw new RuntimeException(cause);
        }
    }

    class CallableCreate implements Callable<V> {

        K key;

        public CallableCreate(K key) {
            this.key = key;
        }

        public V call(){
            // try one more time (a previous future could have come and gone)
            V value = internalGet(key);
            if (value != null){
                return value;
            }

            // create value
            value = create(key);
            if (value == null){
                throw new NullPointerException(
                        "create(K) returned null for: " + key);
            }
            return value;
        }
    }
}
