package com.regnosys.rosetta.generator.java.util;

import java.lang.reflect.Method;

public class PreferWildcardImportMethod {
	private final Method method;

	public PreferWildcardImportMethod(Method method) {
		this.method = method;
	}
	
	public Method getMethod() {
		return method;
	}
}
