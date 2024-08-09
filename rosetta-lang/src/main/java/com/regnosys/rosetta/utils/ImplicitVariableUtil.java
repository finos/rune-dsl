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

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.regnosys.rosetta.rosetta.translate.TranslationParameter;
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
	public Optional<? extends EObject> findObjectDefiningImplicitVariable(EObject context) {
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
			} else if (container instanceof RosettaRule) {
				return Optional.of(container);
			} else if (container instanceof Translation) {
				Translation trans = (Translation)container;
				return findFirstUnnamedParameter(trans.getParameters());
			}
			prev = container;
		}
		return Optional.empty();
	}
	private Optional<TranslationParameter> findFirstUnnamedParameter(List<TranslationParameter> params) {
		return params.stream().filter(p -> p.getName() == null).findFirst();
	}
	
	/**
	 * Indicates whether an implicit variable exists in the given context.
	 */
	public boolean implicitVariableExistsInContext(EObject context) {
		return findObjectDefiningImplicitVariable(context).isPresent();
	}
}
