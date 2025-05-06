package com.regnosys.rosetta.generator.java.scoping;

import com.rosetta.util.types.JavaTypeDeclaration;

public interface JavaScope {
	JavaPackageScope getPackageScope(JavaPackageName packageName);
	JavaClassScope getClassScope(JavaTypeDeclaration<?> clazz);
}
