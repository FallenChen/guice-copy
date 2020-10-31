package org.garry.gucie_clone.inject.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concurrent hash map that wraps keys and/or values in soft or weak references.
 * Does not support null keys or values. Uses identity equality for weak and soft keys
 *
 * <p>The concurrent semantics of {@link ConcurrentHashMap} combined with the
 * fact that the garbage collector can asynchronously reclaim and clean up
 * after keys and values at any time can lead to some racy semantics. For
 * example, {@link #size()} returns an upper bound on the size, i.e. the actual
 * size may be smaller in cases where the key or value has been reclaimed but
 * the map entry has not been cleaned up yet.
 *
 * <p>Another example: If {@link #get(Object)} cannot find an existing entry
 * for a key, it will try to create one. This operation is not atomic. One
 * thread could {@link #put(Object, Object)} a value between the time another
 * thread running {@code get()} checks for an entry and decides to create one.
 * In this case, the newly created value will replace the put value in the
 * map. Also, two threads running {@code get()} concurrently can potentially
 * create duplicate values for a given key.
 *
 * <p>In other words, this class is great for caching but not atomicity.
 *
 * @param <K>
 * @param <V>
 */
public class ReferenceMap<K, V> implements Map<K,V>, Serializable {

    private static final long serialVersionUID = 0;

    transient ConcurrentHashMap<Object,Object> delegate;

    final ReferenceType keyReferenceType;
    final ReferenceType valueReferenceType;

    /**
     * Concurrent hash map that wraps keys and/or values based on specified reference types
     * @param keyReferenceType
     * @param valueReferenceType
     */
    public ReferenceMap(ReferenceType keyReferenceType, ReferenceType valueReferenceType) {
        ensureNotNull(keyReferenceType,valueReferenceType);
        if (keyReferenceType == ReferenceType.PHANTOM || valueReferenceType == ReferenceType.PHANTOM){
            throw new IllegalArgumentException("Phantom references not supported.");
        }

        this.delegate = new ConcurrentHashMap<Object, Object>();
        this.keyReferenceType = keyReferenceType;
        this.valueReferenceType = valueReferenceType;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
