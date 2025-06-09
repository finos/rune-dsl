package com.regnosys.rosetta.generator.java.scoping;

import java.util.HashMap;
import java.util.Map;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se17/html/jls-6.html#jls-6.3
 * 
 *   The package `java` is always in scope.
 */
public class JavaGlobalScope extends AbstractJavaScope<JavaGlobalScope> {
	private final Map<DottedPath, JavaPackageScope> packages = new HashMap<>();
	// TODO: add existing packages (rosetta-runtime)
	// TODO: auto-import java.lang in importing string concatenation client
	
	public JavaGlobalScope() {
		super("Global Java scope");
	}
	
	public JavaClassScope createClassScopeAndIdentifier(JavaTypeDeclaration<?> clazz) {
		JavaPackageScope packageScope = getOrCreatePackageScope(clazz.getPackageName());
		return packageScope.createClassScope(clazz);
	}
	
	@Override
	public JavaClassScope getClassScope(JavaTypeDeclaration<?> clazz) {
		return getOrCreatePackageScope(clazz.getPackageName()).getClassScopeInPackage(clazz);
	}
	@Override
	public JavaPackageScope getPackageScope(JavaPackageName packageName) {
		return getOrCreatePackageScope(packageName.getName());
	}
	
	private JavaPackageScope getOrCreatePackageScope(DottedPath packageName) {
		return packages.computeIfAbsent(packageName, (p) -> new JavaPackageScope(p, this));
	}
}
