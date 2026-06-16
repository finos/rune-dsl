package com.regnosys.rosetta.generator.java;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.types.RObject;
import com.rosetta.util.types.JavaTypeDeclaration;

/**
 * An {@link XtendJavaClassGenerator} for objects with an EMF source object.
 *
 * @deprecated New generators should extend {@link FluentRObjectJavaClassGenerator}.
 * This class will be removed once all generators are migrated to the fluent API.
 */
@Deprecated
public abstract class RObjectJavaClassGenerator<T extends RObject, C extends JavaTypeDeclaration<?>> extends XtendJavaClassGenerator<T, C> {
	@Override
	protected EObject getSource(T object) {
		return object.getEObject();
	}
}
