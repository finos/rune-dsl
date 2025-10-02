/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.utils;

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.BuiltinAnnotationWithPath;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;

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
	public Optional<? extends EObject> findContainerDefiningImplicitVariable(EObject context) {
		Iterable<EObject> containers = EcoreUtil2.getAllContainers(context);
		EObject prev = context;
		for (EObject container: containers) {
			if (container instanceof Condition cond) {
				RosettaTypeWithConditions enclosingType = cond.getEnclosingType();
				if (enclosingType != null) {
					return Optional.of(enclosingType);
				}
			} else if (container instanceof RosettaFunctionalOperation op) {
                InlineFunction f = op.getFunction();
				if (f != null && f.equals(prev) && f.getParameters().isEmpty()) {
					return Optional.of(container);
				}
			} else if (container instanceof RosettaRule) {
				return Optional.of(container);
			} else if (container instanceof SwitchCaseOrDefault c) {
                if (!c.isDefault() && (c.getGuard().getChoiceOptionGuard() != null || c.getGuard().getDataGuard() != null)) {
					return Optional.of(container);
				}
			} else if (container instanceof BuiltinAnnotationWithPath) {
				Attribute attr = EcoreUtil2.getContainerOfType(container, Attribute.class);
				if (attr != null) {
					return Optional.of(attr);
				}
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
