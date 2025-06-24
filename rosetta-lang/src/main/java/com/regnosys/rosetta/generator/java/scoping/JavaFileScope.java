package com.regnosys.rosetta.generator.java.scoping;

import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaFileScope extends AbstractJavaScope<JavaPackageScope> {

	protected JavaFileScope(String fileName, JavaPackageScope parentScope) {
		super("File[" + fileName + "]", parentScope);
	}

	public JavaClassScope createClassScope(JavaTypeDeclaration<?> clazz) {
		JavaClassScope superClassScope = null;
		if (clazz.getSuperclassDeclaration() != null) {
			superClassScope = getClassScope(clazz.getSuperclassDeclaration());
		}
		return new JavaClassScope(clazz.getSimpleName(), this, superClassScope);
	}
}
