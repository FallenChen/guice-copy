package org.garry.gucie_clone.inject.util;

/**
 * Reference type. Used to specify what type of reference to keep to a referent
 */
public enum ReferenceType {

    /**
     * Prevents referent from being reclaimed by the garbage collector
     */
    STRONG,

    /**
     * Referent reclaimed in an LRU fashion when VM runs low on memory and no strong references exist
     * @see java.lang.ref.SoftReference
     */
    SOFT,

    /**
     * Referent reclaimed when no strong or soft references exist
     *
     * @see java.lang.ref.WeakReference
     */
    WEAK,

    /**
     * Similar to weak reference except the garbage collector doesn't actually
     * reclaim the referent. More flexible alternative to finalization
     *
     * @see java.lang.ref.PhantomReference
     */
    PHANTOM;
}
