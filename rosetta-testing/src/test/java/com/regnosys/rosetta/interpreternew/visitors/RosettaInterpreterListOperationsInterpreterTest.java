package com.regnosys.rosetta.interpreternew.visitors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterNew;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
class RosettaInterpreterListOperationsInterpreterTest {
	
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	@Inject
	private ExpressionValidationHelper validation;
	
	private ExpressionFactory expFactory;
	
	@BeforeEach
	public void setup() {
		expFactory = ExpressionFactoryImpl.init();
	}
	
	private void testHelper(RosettaInterpreterValue expected, String[] expressions) {
		List<RosettaExpression> expressionsParsed = Stream.of(expressions)
				.map(x -> parser.parseExpression(x))
				.collect(Collectors.toList());
		expressionsParsed.stream().forEach(x -> validation.assertNoIssues(x));
		expressionsParsed.stream().forEach(x -> {
			assertEquals(expected,
					interpreter.interp(x));
		});
	}
	
	@Test
	void testInterpContains() {
		String[] expressionsTrue = new String[] {
				"[1,2,3] contains [1,2]",
				"[1] contains 1",
				"[2, [3]] contains [2, 3]",
				"[2] contains [2, 2]",
				"False contains False"
		};
		testHelper(new RosettaInterpreterBooleanValue(true), expressionsTrue);
		
		String[] expressionsFalse = new String[] {
				"[1,2,3] contains [1,2,3,4]",
				"[] contains 1",
				"[2] contains [2, 3]",
				"[2] contains [2, 2, 3]",
				"[1,2] contains []",
				"[] contains []",
				"[] contains 1",
				"2 contains 1"
		};
		testHelper(new RosettaInterpreterBooleanValue(false), expressionsFalse);
	}
	
	@Test
	void testInterpDisjoint() {
		String[] expressionsTrue = new String[] {
				"[1,2,3] disjoint [4]",
				"[1,2,[3]] disjoint []",
				"[] disjoint [1]",
				"[1,2,3,4] disjoint [0,5,6,7,8]"
		};
		testHelper(new RosettaInterpreterBooleanValue(true), expressionsTrue);
		
		String[] expressionsFalse = new String[] {
				"[1] disjoint [1]",
				"[1,2,3,4] disjoint [4]",
				"[2] disjoint [1,2,3,4,5,6,7]",
				"[1,2,3,4,5,6,7] disjoint [1,2,3,4,5,6,7]"
		};
		testHelper(new RosettaInterpreterBooleanValue(false), expressionsFalse);
	}
	
	@Test
	void testInterpJoin() {
		String msg = "[\"abc\", \"cde\"] join \", \"";
		RosettaExpression msgExp = parser.parseExpression(msg);
		validation.assertNoIssues(msgExp);
		RosettaInterpreterStringValue val =
				(RosettaInterpreterStringValue)interpreter.interp(msgExp);
		assertEquals("abc, cde", val.getValue());
	}

}
