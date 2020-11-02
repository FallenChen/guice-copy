package org.garry.gucie_clone.inject.util;

/**
 * Package-private interface implemented by references that have code to
 * run after garbage collection of their referents
 */
interface FinalizableReference {

    /**
     * Invoked on a background thread after the referent has been
     * garbage collected
     */
    void finalizeReferent();
}
