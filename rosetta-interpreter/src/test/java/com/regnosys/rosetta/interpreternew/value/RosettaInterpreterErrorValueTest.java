package com.regnosys.rosetta.interpreternew.value;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.interpreternew.RosettaInterpreterNew;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
class RosettaInterpreterErrorValueTest {
	
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	
	@SuppressWarnings("unused")
	private ExpressionFactory exFactory;		
	
	RosettaInterpreterError e1;
	RosettaInterpreterError e2;
	RosettaInterpreterError e3;
	RosettaInterpreterError e4;
	RosettaInterpreterErrorValue v1;
	RosettaInterpreterErrorValue v2;
	RosettaInterpreterErrorValue v3;
	RosettaInterpreterBooleanValue vb2;
	RosettaInterpreterBooleanValue vb1;
	List<RosettaInterpreterValue> vals;
	
	RosettaInterpreterBaseError b1;
	RosettaInterpreterBaseError b2;
	List<RosettaInterpreterBaseError> baseErr;
	
	@BeforeEach
	void setup() {
		e1 = new RosettaInterpreterError("e1",null);
		e2 = new RosettaInterpreterError("e2",null);
		e3 = new RosettaInterpreterError("e3",null);
		e4 = new RosettaInterpreterError("e4",null);
		v1 = new RosettaInterpreterErrorValue(e1);
		v2 = new RosettaInterpreterErrorValue(e2);
		v3 = new RosettaInterpreterErrorValue(e3);
		vb1 = new RosettaInterpreterBooleanValue(true);
		vb2 = new RosettaInterpreterBooleanValue(false);
		vals = new ArrayList<>(List.of(v1,v2,v3,vb1,vb2));
		exFactory = ExpressionFactoryImpl.init();		
		b1 = new RosettaInterpreterError("b1", null);
		b2 = new RosettaInterpreterError("b2", null);
		baseErr = new ArrayList<>(List.of(b1,b2));
	}

	@Test
	void testGetErrors() {
		assertEquals(List.of(e1), v1.getErrors());
	}

	@Test
	void testAddError() {
		v1.addError(e2);
		assertEquals(List.of(e1,e2), v1.getErrors());
	}

	@Test
	void testAddAllErrorsRosettaInterpreterErrorValue() {
		v1.addError(e2);
		v3.addAllErrors(v1);
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testAddAllErrorsRosettaInterpreterValue() {
		v1.addError(e2);
		v3.addAllErrors(((RosettaInterpreterValue)v1));
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testAddAllErrorsElistOfRosettaInterpreterBaseError() {
		v3.addAllErrors(List.of(e1,e2));
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testErrorsExistRosettaInterpreterValueRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(v2, vb2));
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vb1, vb2));
	}

	@Test
	void testErrorsExistRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(v2));
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vb1));
	}

	@Test
	void testErrorsExistListOfRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(vals));
		vals.remove(v1);
		vals.remove(v2);
		vals.remove(v3);
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vals));
	}

	@Test
	void testMergeListOfRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue();
		val.addAllErrors(List.of(e1,e2,e3));
		
		assertEquals(val, RosettaInterpreterErrorValue.merge(vals));
	}

	@Test
	void testMergeRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue(e2);
		assertEquals(val, RosettaInterpreterErrorValue.merge(v2));
	}

	@Test
	void testMergeRosettaInterpreterValueRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue();
		val.addAllErrors(List.of(e2, e3));
		
		assertEquals(val, RosettaInterpreterErrorValue.merge(v2, v3));
	}
	
	@Test
	void sumErrorNewTest() {
		RosettaExpression ex = parser.parseExpression("5 + 5 + 3 + ([] sum)");
		RosettaInterpreterValue val = interpreter.interp(ex);
		assertTrue(val instanceof RosettaInterpreterErrorValue);
		assertEquals("Error at line 1, position 12: \"([] sum)\". Cannot take sum of empty list",
				((RosettaInterpreterErrorValue)val).getErrors().get(0).toString());
	}
	
	void mergeBadWeatherTest() {
		List<RosettaInterpreterValue> list = new ArrayList<>();
		list.add(vb1);
		list.add(vb2);
		
		assertThrows(IllegalArgumentException.class, () -> {
			RosettaInterpreterErrorValue.merge(list);
		});
	}

	@Test
	void hashTest() {
		RosettaInterpreterErrorValue err1 = new RosettaInterpreterErrorValue(baseErr);
		RosettaInterpreterErrorValue err2 = new RosettaInterpreterErrorValue(baseErr);
		
		assertEquals(err1.hashCode(), err2.hashCode());
	}
	
	@Test
	void toStringTest() {
		assertEquals(
				"RosettaInterpreterErrorValue [errors=[e1]]", 
				v1.toString());
	}
	
	@Test
	void streamElementTest() {
		List<Object> result = new ArrayList<Object>();
		result.add(e1);
		
		assertEquals(result, v1.toElementStream().collect(Collectors.toList()).get(0));
	}
	
	@Test
	void equalsBadWeatherTest() {
		assertFalse(v1.equals(null));
		assertFalse(v1.equals("error"));
	}
}
