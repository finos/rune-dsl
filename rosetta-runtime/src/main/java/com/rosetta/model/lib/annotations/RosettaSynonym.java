package com.rosetta.model.lib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(RosettaSynonyms.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RosettaSynonym {
	
	String value() ;
	String source() ;
	String path()  default "";
	int maps() default 1;

}
