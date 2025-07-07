package com.regnosys.rosetta.generator.java.scoping;

import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaClassScope extends AbstractJavaScope<AbstractJavaScope<?>> {	
	private JavaClassScope(String className, AbstractJavaScope<?> parentScope) {
		super("Class[" + className + "]", parentScope);
	}
	
	public static JavaClassScope createAndRegisterIdentifier(JavaTypeDeclaration<?> clazz) {
		JavaFileScope fileScope = new JavaFileScope(clazz.getSimpleName() + ".java", clazz.getPackageName());
		fileScope.createIdentifier(clazz, clazz.getSimpleName());
		return new JavaClassScope(clazz.getSimpleName(), fileScope);
	}
	
	public JavaClassScope createNestedClassScopeAndRegisterIdentifier(JavaTypeDeclaration<?> clazz) {
		this.createIdentifier(clazz, clazz.getSimpleName());
		this.getFileScope().createIdentifier(clazz, clazz.getNestedTypeName().withDots());

		return new JavaClassScope(clazz.getSimpleName(), this);
	}

	public JavaMethodScope createMethodScope(String methodName) {
		return new JavaMethodScope(methodName, this);
	}

	@Override
	public String escapeName(String name) {
		// How should members be escaped?
		return "_" + name;
	}
}
