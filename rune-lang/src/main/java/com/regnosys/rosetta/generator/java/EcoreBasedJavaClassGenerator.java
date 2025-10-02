package com.regnosys.rosetta.generator.java;

import org.eclipse.emf.ecore.EObject;

import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class EcoreBasedJavaClassGenerator<T extends EObject, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
	@Override
	protected EObject getSource(T object) {
		return object;
	}
}
