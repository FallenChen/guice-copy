package org.garry.gucie_clone.inject.util;

import java.lang.ref.SoftReference;

/**
 * Soft reference with a {@link #finalizeReferent()} method which a background
 * thread invokes after the garbage collector reclaims the referent. This is
 * a simpler alternative to using a {@link java.lang.ref.ReferenceQueue}
 * @param <T>
 */
public abstract class FinalizableSoftReference<T> extends SoftReference<T> implements FinalizableReference {

    protected FinalizableSoftReference(T referent){
        super(referent,FinalizableReferenceQueue.getInstance());
    }
}
