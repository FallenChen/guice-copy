package org.garry.gucie_clone.inject;

import java.lang.reflect.Member;

/**
 * Context of the current injection
 */
public interface Context {

    /**
     * Gets the {@link Container}
     * @return
     */
    Container getContainer();

    Member getMember();

    /**
     * Gets the type of the field or parameter which is being injected
     * @return
     */
    Class<?> getType();

    /**
     * Gets the name of the injection
     * @return
     */
    String getName();
}
