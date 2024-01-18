package com.regnosys.rosetta.tests.extensions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.xtext.ISetup;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@Inherited
@Documented
public @interface WithSetup {
	/**
	 * the ISetup class which will be used to create an {@link com.google.inject.Injector Injector}.
	 */
	Class<? extends ISetup> value();
}
