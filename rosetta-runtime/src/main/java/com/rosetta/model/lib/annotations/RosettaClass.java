package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RosettaClass {
	
	String value()  default "";
}
