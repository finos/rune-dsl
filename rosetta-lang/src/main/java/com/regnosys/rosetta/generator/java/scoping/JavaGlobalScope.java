package com.regnosys.rosetta.generator.java.scoping;

import java.util.HashMap;
import java.util.Map;

import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se17/html/jls-6.html#jls-6.3
 * 
 *   The package `java` is always in scope.
 */
public class JavaGlobalScope extends AbstractJavaScope<JavaGlobalScope> {
	private final Map<DottedPath, JavaPackageScope> packages = new HashMap<>();
	
	public JavaGlobalScope() {
		super("Global Java scope");
	}
	
	public void initializeRuntimeScopes(JavaTypeUtil typeUtil) {
		JavaClassScope rosettaModelObjectScope = createClassScopeAndRegisterIdentifier(typeUtil.ROSETTA_MODEL_OBJECT);
		rosettaModelObjectScope.createUniqueIdentifier("getType");
		rosettaModelObjectScope.createUniqueIdentifier("getValueType");
	}
	
	public JavaClassScope createClassScopeAndRegisterIdentifier(JavaTypeDeclaration<?> clazz) {
		JavaPackageScope packageScope = getOrCreatePackageScope(clazz.getPackageName());
		return packageScope.createClassScopeAndRegisterIdentifier(clazz);
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
	
	@Override
	public boolean isNameTaken(String desiredName) {
		if (super.isNameTaken(desiredName)) {
			return true;
		}
		try {
			Class.forName("java.lang." + desiredName);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
