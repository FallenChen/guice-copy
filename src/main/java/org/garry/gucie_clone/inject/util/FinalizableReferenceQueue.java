package org.garry.gucie_clone.inject.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts a background thread that cleans up after reclaimed referents
 */
class FinalizableReferenceQueue extends ReferenceQueue<Object> {

    private static final Logger logger =
            Logger.getLogger(FinalizableReferenceQueue.class.getName());

    private FinalizableReferenceQueue(){};

    void cleanUp(Reference reference){
        try {
            ((FinalizableReference)reference).finalizeReferent();
        }catch (Throwable t){
            deliverBadNews(t);
        }
    }

    void deliverBadNews(Throwable t){
        logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
    }

    void start(){
        Thread thread = new Thread("FinalizableReferenceQueue"){
            @Override
            public void run() {
               try {
                   cleanUp(remove());
               }catch (InterruptedException e){

               }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    static ReferenceQueue<Object> instance = createAndStart();

    static FinalizableReferenceQueue createAndStart() {
        FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
        queue.start();
        return queue;
    }

    /**
     * Gets instance
     * @return
     */
    public static ReferenceQueue<Object> getInstance(){
        return instance;
    }
}
