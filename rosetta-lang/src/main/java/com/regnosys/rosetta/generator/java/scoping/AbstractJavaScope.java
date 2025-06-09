package com.regnosys.rosetta.generator.java.scoping;

import javax.lang.model.SourceVersion;

import com.regnosys.rosetta.generator.GeneratorScope;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class AbstractJavaScope<T extends AbstractJavaScope<?>> extends GeneratorScope<T> implements JavaScope {

	protected AbstractJavaScope(String description) {
		super(description);
	}
	protected AbstractJavaScope(String description, T parentScope) {
		super(description, parentScope);
	}

	@Override
	public boolean isValidIdentifier(String name) {
		return SourceVersion.isName(name);
	}
	
	@Override
	public JavaClassScope getClassScope(JavaTypeDeclaration<?> clazz) {
		return getParent().getClassScope(clazz);
	}
	@Override
	public JavaPackageScope getPackageScope(JavaPackageName packageName) {
		return getParent().getPackageScope(packageName);
	}
}
