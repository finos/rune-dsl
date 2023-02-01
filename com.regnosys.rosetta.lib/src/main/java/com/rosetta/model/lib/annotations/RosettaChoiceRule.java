package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// @Compat. This is not used anymore. All conditions are now `RosettaDataRule`s.
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RosettaChoiceRule {
	
	String value()  default "";
}
