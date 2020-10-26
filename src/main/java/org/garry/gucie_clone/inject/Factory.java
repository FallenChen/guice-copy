package org.garry.gucie_clone.inject;

public interface Factory<T>{

    /**
     * Creates an object to be injected
     * @param context of this injection
     * @return instance to be injected
     * @throws Exception
     */
    T create(Context context) throws Exception;
}
