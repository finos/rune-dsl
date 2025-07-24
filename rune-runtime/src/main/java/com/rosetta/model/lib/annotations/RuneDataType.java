package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RuneDataType {
    String value() default "";

    String model() default "";

    Class<? extends RosettaModelObjectBuilder> builder();

    String version() default "";
}
