package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaFeatureCallImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterFeatureCallTest {

	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Inject 
	ModelHelper mh;
	
	@Test
	public void testDate() {
		RosettaExpression expr = parser.parseExpression("date { day: 5, month: 7, year: 2000 } -> day");
		
		System.out.println(((RosettaFeatureCallImpl) expr).getFeature().getGetNameOrDefault());
//		RosettaInterpreterValue result = interpreter.interp(expr);
//		
//		assertEquals(5, ((RosettaInterpreterDateValue) result).getDay());
//		assertEquals(7, ((RosettaInterpreterDateValue) result).getMonth());
//		assertEquals(2000, ((RosettaInterpreterDateValue) result).getYear());
	}
}
