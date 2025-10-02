package com.regnosys.rosetta.generator.java.scoping;

import javax.lang.model.SourceVersion;

import com.regnosys.rosetta.generator.GeneratorScope;

public abstract class AbstractJavaScope<T extends AbstractJavaScope<?>> extends GeneratorScope<T> {

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
	
	public JavaFileScope getFileScope() {
		if (getParent() == null) {
			return null;
		}
		return getParent().getFileScope();
	}
}
