package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(RosettaMetaAttributes.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RosettaMetaAttribute {
	String value();
}
