package org.garry.gucie_clone.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.garry.gucie_clone.inject.Container.DEFAULT_NAME;

/**
 * annotates members and parameters which should have their value[s] injected
 */
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject
{
    String value() default DEFAULT_NAME;

    boolean required() default true;
}
