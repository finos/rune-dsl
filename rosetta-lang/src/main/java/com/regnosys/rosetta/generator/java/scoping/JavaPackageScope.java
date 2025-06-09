package com.regnosys.rosetta.generator.java.scoping;

import java.util.HashMap;
import java.util.Map;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaPackageScope extends AbstractJavaScope<JavaGlobalScope> {
	private final Map<JavaTypeDeclaration<?>, JavaClassScope> classScopes = new HashMap<>();
	private final Map<JavaTypeDeclaration<?>, JavaFileScope> fileScopes = new HashMap<>();
	
	JavaPackageScope(DottedPath packageName, JavaGlobalScope parentScope) {
		super("Package[" + packageName.withDots() + "]", parentScope);
	}

	public JavaClassScope createClassScopeAndIdentifier(JavaTypeDeclaration<?> clazz) {
		this.createIdentifier(clazz, clazz.getSimpleName());
		JavaFileScope fileScope = new JavaFileScope(clazz.getCanonicalName().withForwardSlashes() + ".java", this);
		this.fileScopes.put(clazz, fileScope);
		return fileScope;
		
		if (this.classScopes.containsKey(clazz)) {
			throw new IllegalStateException("There is already a class scope defined for class `" + clazz.getCanonicalName() + "`.\n" + this);
		}
		JavaTypeDeclaration<?> superClass = clazz.getSuperclassDeclaration();
		JavaClassScope superClassScope = null;
		if (superClass != null) {
			superClassScope = getClassScope(superClass);
		}
		JavaClassScope classScope = new JavaClassScope(clazz.getSimpleName(), this, superClassScope);
		this.classScopes.put(clazz, classScope);
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
