package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(RosettaStereotypes.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RosettaStereotype {
	
	String value()  default "";
}
