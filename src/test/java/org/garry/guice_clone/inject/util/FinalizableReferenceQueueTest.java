package org.garry.guice_clone.inject.util;

import junit.framework.TestCase;
import org.garry.gucie_clone.inject.util.FinalizableWeakReference;

public class FinalizableReferenceQueueTest extends TestCase {

    public void testFinalizeReferentCalled(){
        MockReference reference = new MockReference();
        reference.enqueue();
        // wait up to 5s
        for (int i = 0; i < 50; i++) {
            if (reference.finalizeReferentCalled){
                return;
            }
            try {
                Thread.sleep(10);
            }catch (InterruptedException e){};
        }
        fail();
    }


    static class MockReference extends FinalizableWeakReference<Object> {

        boolean finalizeReferentCalled;

        public MockReference() {
            super(new Object());
        }

        @Override
        public void finalizeReferent() {
            finalizeReferentCalled = true;
        }
    }
}
