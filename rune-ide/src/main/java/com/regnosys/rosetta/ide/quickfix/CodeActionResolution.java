package com.regnosys.rosetta.ide.quickfix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface CodeActionResolution {
	/**
     * Specifies the title of the code action for which the annotated method provides a resolution.
     * 
     * @return the title of the associated code action.
     */
	String value();
}
