package com.regnosys.rosetta.generator.java;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.types.RObject;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class RObjectJavaClassGenerator<T extends RObject, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
	@Override
	protected EObject getSource(T object) {
		return object.getEObject();
	}
}
