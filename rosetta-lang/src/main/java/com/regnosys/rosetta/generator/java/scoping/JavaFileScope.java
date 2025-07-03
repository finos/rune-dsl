package com.regnosys.rosetta.generator.java.scoping;

import java.util.ArrayList;
import java.util.List;

import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaFileScope extends AbstractJavaScope<JavaPackageScope> {

	protected JavaFileScope(String fileName, JavaPackageScope parentScope) {
		super("File[" + fileName + "]", parentScope);
	}

	public JavaClassScope createClassScope(JavaTypeDeclaration<?> clazz) {
		List<JavaClassScope> superClassScopes = new ArrayList<>();
//		if (clazz.getSuperclassDeclaration() != null) {
//			superClassScopes.add(getClassScope(clazz.getSuperclassDeclaration()));
//		}
//		for (var interf : clazz.getInterfaceDeclarations()) {
//			superClassScopes.add(getClassScope(interf));
//		}
		return new JavaClassScope(clazz.getSimpleName(), this, superClassScopes);
	}
	
	@Override
	public JavaFileScope getFileScope() {
		return this;
	}
}
