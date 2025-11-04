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

package com.regnosys.rosetta.serialization;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.parsetree.reconstr.impl.DefaultTransientValueService;

import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;

public class RosettaTransientValueService extends DefaultTransientValueService {
	private final Set<EStructuralFeature> ignoredFeatures;

	public RosettaTransientValueService() {
		EStructuralFeature generatedInputWasSetFeature = ExpressionPackage.eINSTANCE
				.getHasGeneratedInput_GeneratedInputWasSet();
		EStructuralFeature implicitVariableIsInContextFeature = ExpressionPackage.eINSTANCE
				.getRosettaCallableReference_ImplicitVariableIsInContext();
		EStructuralFeature hardcodedConditionFeature = SimplePackage.eINSTANCE.getChoice__hardcodedConditions();
		EStructuralFeature hardcodedNameFeature = SimplePackage.eINSTANCE.getChoiceOption__hardcodedName();
		EStructuralFeature hardcodedCardinalityFeature = SimplePackage.eINSTANCE
				.getChoiceOption__hardcodedCardinality();
		ignoredFeatures = Set.of(generatedInputWasSetFeature, implicitVariableIsInContextFeature,
				hardcodedConditionFeature, hardcodedNameFeature, hardcodedCardinalityFeature);

	}

	@Override
	public boolean isCheckElementsIndividually(EObject owner, EStructuralFeature feature) {
		return true;
	}

	@Override
	public boolean isTransient(EObject owner, EStructuralFeature feature, int index) {
		if (super.isTransient(owner, feature, index)) {
			return true;
		}
		if (ignoredFeatures.contains(feature)) {
			return true;
		}
		Object value = owner.eGet(feature);
		if (index >= 0 && value instanceof List<?>) {
			value = ((List<?>) value).get(index);
		}
		if (value instanceof RosettaExpression && ((RosettaExpression) value).isGenerated()) {
			return true;
		}
		return false;
	}

}
