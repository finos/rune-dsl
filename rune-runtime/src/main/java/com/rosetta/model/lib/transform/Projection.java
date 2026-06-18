package com.rosetta.model.lib.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a generated function class as performing a projection, declaring the outbound
 * (de)serialization format and, optionally, the schema id and config file that configure it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Projection {
    /** The schema id (the name of the referenced {@code schema}), or empty for a bare format. */
    String id() default "";

    /** The outbound serialization format. */
    SerializationFormat format();

    /** The classpath location of the format's configuration file, or empty if there is none. */
    String configPath() default "";
}
