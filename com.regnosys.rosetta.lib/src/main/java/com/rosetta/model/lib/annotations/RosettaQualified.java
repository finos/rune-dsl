package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.rosetta.model.lib.RosettaModelObject;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Deprecated // remove once all model off version 4.42.1
public @interface RosettaQualified {
	
	String attribute();
	Class<? extends RosettaModelObject> qualifiedClass();
}
