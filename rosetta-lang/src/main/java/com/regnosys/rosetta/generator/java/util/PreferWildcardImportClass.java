package com.regnosys.rosetta.generator.java.util;

import com.rosetta.util.types.JavaClass;

public class PreferWildcardImportClass {
	private final JavaClass javaClass;

	public PreferWildcardImportClass(JavaClass javaClass) {
		this.javaClass = javaClass;
	}
	
	public JavaClass getJavaClass() {
		return javaClass;
	}
}
