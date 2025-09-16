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

import java.util.Collections;
import java.util.List;
import java.util.Stack;
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

public class ExpressionHelper {
	public boolean usesOutputParameter(RosettaExpression expr) {
		return !this.findOutputRef(expr, new Stack<String>()).isEmpty();
	}

	public List<RosettaSymbol> findOutputRef(EObject ele, Stack<String> trace) {
		if (ele == null) {
			return Collections.emptyList();
		}
		if (ele instanceof ShortcutDeclaration) {
			ShortcutDeclaration sd = (ShortcutDeclaration)ele;
			trace.push(sd.getName());
			List<RosettaSymbol> result = findOutputRef(sd.getExpression(), trace);
			if (result.isEmpty())
				trace.pop();
			return result;
		} else if (ele instanceof RosettaSymbolReference && !(((RosettaSymbolReference)ele).getSymbol() instanceof RosettaCallableWithArgs)) {
			RosettaSymbolReference cc = (RosettaSymbolReference)ele;
			if (cc.getSymbol() instanceof Attribute &&
					cc.getSymbol().eContainingFeature() == SimplePackage.Literals.FUNCTION__OUTPUT)
				return Collections.singletonList(cc.getSymbol());
			return findOutputRef(cc.getSymbol(), trace);
		} else {
			return Streams.concat(
						ele.eContents().stream(),
						ele.eCrossReferences().stream()
							.filter(ref -> (ref instanceof RosettaExpression) || (ref instanceof ShortcutDeclaration)))
					.<RosettaSymbol>flatMap(ref -> this.findOutputRef(ref, trace).stream())
					.collect(Collectors.toList());
		}
	}

	public RosettaExpression getParentExpression(RosettaExpression e) {
		if (e instanceof RosettaFeatureCall) {
			return ((RosettaFeatureCall)e).getReceiver();
		} else {
			return null;
		}
	}
}
