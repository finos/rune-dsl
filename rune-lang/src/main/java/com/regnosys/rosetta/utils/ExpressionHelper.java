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

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import org.eclipse.xtext.EcoreUtil2;

public class ExpressionHelper {
	public boolean usesOutputParameter(RosettaExpression expr) {
		return usesOutputParameter(expr, new HashSet<>());
	}
	private boolean usesOutputParameter(RosettaExpression expr, Set<RosettaExpression> visited) {
		return findFirstReferenceToOutput(expr, visited) != null;
	}
	public RosettaSymbolReference findFirstReferenceToOutput(RosettaExpression expr) {
		return findFirstReferenceToOutput(expr, new HashSet<>());
	}
	private RosettaSymbolReference findFirstReferenceToOutput(RosettaExpression expr, Set<RosettaExpression> visited) {
		if (!visited.add(expr)) {
			return null;
		}
		return EcoreUtil2.eAllOfType(expr, RosettaSymbolReference.class)
				.stream().map(ref -> {
					RosettaSymbol symbol = ref.getSymbol();
					if (symbolIsOutputParameter(symbol)) {
						return ref;
					}
					if (symbol instanceof ShortcutDeclaration sd) {
						return findFirstReferenceToOutput(sd.getExpression(), visited);
					}
					return null;
				})
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}
	private boolean symbolIsOutputParameter(RosettaSymbol symbol) {
		return symbol.eContainingFeature() == SimplePackage.Literals.FUNCTION__OUTPUT;
	}

	public RosettaExpression getParentExpression(RosettaExpression e) {
		if (e instanceof RosettaFeatureCall) {
			return ((RosettaFeatureCall)e).getReceiver();
		} else {
			return null;
		}
	}
}
