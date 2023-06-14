package com.regnosys.rosetta.utils;

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.simple.Data;

/**
 * A tool for finding information about implicit variables, often called
 * `this`, `item`, `it`, ...
 */
public class ImplicitVariableUtil {
	
	public RosettaImplicitVariable getDefaultImplicitVariable() {
		RosettaImplicitVariable def = ExpressionFactory.eINSTANCE.createRosettaImplicitVariable();
		def.setName("item");
		def.setGenerated(true);
		return def;
	}
	
	/**
	 * Find the enclosing object that defines the implicit variable in the given expression.
	 */
	public Optional<EObject> findContainerDefiningImplicitVariable(EObject context) {
		Iterable<EObject> containers = EcoreUtil2.getAllContainers(context);
		EObject prev = context;
		for (EObject container: containers) {
			if (container instanceof Data) {
				return Optional.of(container);
			} else if (container instanceof RosettaFunctionalOperation) {
				RosettaFunctionalOperation op = (RosettaFunctionalOperation)container;
				InlineFunction f = op.getFunction();
				if (f != null && f.equals(prev) && f.getParameters().size() == 0) {
					return Optional.of(container);
				}
			} else if (container instanceof RosettaBlueprint) {
				return Optional.of(container);
			}
			prev = container;
		}
		return Optional.empty();
	}
	
	/**
	 * Indicates whether an implicit variable exists in the given context.
	 */
	public boolean implicitVariableExistsInContext(EObject context) {
		return findContainerDefiningImplicitVariable(context).isPresent();
	}
}
