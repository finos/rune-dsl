package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RuneAttribute {

	String value() default "";
	/**
	 * @deprecated use @Required annotation instead. Deprecated since 9.76.1.
	 */
	@Deprecated
	boolean isRequired() default false;
}
