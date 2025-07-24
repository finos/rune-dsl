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

package com.regnosys.rosetta.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.extensions.InjectionExtension;

import org.eclipse.xtext.testing.InjectWith;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper;
import com.regnosys.rosetta.tests.util.RosettaValueHelper;
import com.rosetta.model.lib.RosettaNumber;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaInterpreterTest {
	@Inject
	private ExpressionParser parser;
	@Inject
	private ExpressionValidationHelper validation;
	@Inject
	private RosettaInterpreter interpreter;
	@Inject
	private RosettaValueHelper helper;
	
	private RosettaValue interpret(CharSequence expression) {
		return interpret(expression, Collections.emptyList(), Collections.emptyMap());
	}
	private RosettaValue interpret(CharSequence expression, String var1, String type1, Object value1) {
		return interpret(expression, List.of(var1 + " " + type1), Map.of(var1, helper.toValue(value1)));
	}
	private RosettaValue interpret(CharSequence expression, String var1, String type1, Object value1, String var2, String type2, Object value2) {
		return interpret(expression, List.of(var1 + " " + type1, var2 + " " + type2), Map.of(var1, helper.toValue(value1), var2, helper.toValue(value2)));
	}
	private RosettaValue interpret(CharSequence expression, List<String> attributes, Map<String, RosettaValue> attributeValues) {
		RosettaExpression expr = parser.parseExpression(expression, attributes);
		validation.assertNoIssues(expr);
		return interpreter.interpret(expr, RosettaInterpreterContext.of(attributeValues));
	}
	
	@Test
	public void testAddition() {
		assertEquals(interpret("1 + 2"), RosettaNumberValue.of(RosettaNumber.valueOf(3)));
		assertEquals(interpret("a + 2", "a", "int (1..1)", 42), RosettaNumberValue.of(RosettaNumber.valueOf(44)));
		assertEquals(interpret("a + b", "a", "number (1..1)", 42, "b", "int (1..1)", -42), RosettaNumberValue.of(RosettaNumber.ZERO));
		assertEquals(interpret("a + b", "a", "string (1..1)", "AA", "b", "string (1..1)", "BB"), RosettaStringValue.of("AABB"));
	}
}
