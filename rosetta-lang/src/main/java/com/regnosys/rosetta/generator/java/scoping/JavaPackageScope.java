package com.regnosys.rosetta.generator.java.scoping;

import java.util.HashMap;
import java.util.Map;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaPackageScope extends AbstractJavaScope<JavaGlobalScope> {
	private final Map<JavaTypeDeclaration<?>, JavaClassScope> classScopes = new HashMap<>();
	
	JavaPackageScope(DottedPath packageName, JavaGlobalScope parentScope) {
		super("Package[" + packageName.withDots() + "]", parentScope);
	}

	public JavaClassScope createClassScopeAndRegisterIdentifier(JavaTypeDeclaration<?> clazz) {
		if (this.classScopes.containsKey(clazz)) {
			throw new IllegalStateException("There is already a class scope defined for class `" + clazz.getCanonicalName() + "`.\n" + this);
		}
		this.createIdentifier(clazz, clazz.getSimpleName());
		JavaFileScope fileScope = new JavaFileScope(clazz.getCanonicalName().withForwardSlashes() + ".java", this);
		JavaClassScope classScope = fileScope.createClassScope(clazz);
		classScopes.put(clazz, classScope);
		return classScope;
	}
	public JavaClassScope getClassScopeInPackage(JavaTypeDeclaration<?> clazz) {
		if (!this.classScopes.containsKey(clazz)) {
			throw new IllegalStateException("No scope defined for class `" + clazz.getCanonicalName() + "`.\n" + this);
		}
		return this.classScopes.get(clazz);
	}
	
	@Override
	public String escapeName(String name) {
		// How should classes be escaped?
		return "_" + name;
	}
}
