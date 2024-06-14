package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterNew;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaConstructorExpressionImpl;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaFeatureCallImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaOnlyExistsExpressionImpl;


@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterOnlyExistsInterpreterTest {
	
	@Inject
	private ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	@Inject
	ModelHelper modelHelper;
	@SuppressWarnings("unused")
	private ExpressionFactory expFactory;
	
	@BeforeEach
	public void setup() {
		expFactory = ExpressionFactoryImpl.init();
	}
	
	@Test
	public void testOptionalAttributeDataType() {
//		RosettaModel model = modelHelper.parseRosettaWithNoErrors("type Person: name string (1..1) height number (0..1)"
//				+ "func M: output: result Person (1..1) set result: Person { name: \"F\", height: empty}");
//		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
//				FunctionImpl) model.getElements().get(1)).getOperations().get(0).getExpression());
//		RosettaInterpreterTypedValue result = (RosettaInterpreterTypedValue) interpreter.interp(constructor);

		
//		Example from Data Types Class:
//		RosettaModel model = mh.parseRosettaWithNoErrors("type Person: name string (1..1) " 
//				+ "type Age extends Person: age number (1..1) "
//				+ "func M: output: result number (1..1) set result: "
//				+ "Age { name: \"F\", age: 10 } -> age");
//		RosettaFeatureCallImpl featureCall = ((RosettaFeatureCallImpl) ((FunctionImpl) 
//				model.getElements().get(2)).getOperations().get(0).getExpression());
//		RosettaInterpreterValue result = interpreter.interp(featureCall);
		

//		RosettaModel model = modelHelper.parseRosettaWithNoErrors("type Person: name string (1..1) height number (0..1) " 
//				+ "func M: output: result boolean (1..1) alias person: Person { name: \"F\", height: empty} "
//				+ "set result: person -> name only exists");
//		String code = "type Foo:\n" +
//	              "    bar Bar (0..*)\n" +
//	              "    baz Baz (0..1)\n" +
//	              "\n" +
//	              "type Bar:\n" +
//	              "    before number (0..1)\n" +
//	              "    after number (0..1)\n" +
//	              "    other number (0..1)\n" +
//	              "    beforeList number (0..*)\n" +
//	              "    afterList number (0..*)\n" +
//	              "\n" +
//	              "type Baz:\n" +
//	              "    bazValue number (0..1)\n" +
//	              "    other number (0..1)\n" +
//	              "\n" +
//	              "func OnlyExists:\n" +
//	              "    inputs: foo Foo (1..1)\n" +
//	              "    output: result boolean (1..1)\n" +
//	              "    set result:\n" +
//	              "        foo -> bar -> before only exists\n";
		
		String code = "type Person:\n " +
				"	name string (0..1)\n" +
				"	age int (0..1)\n" +
				"\n" +
				"func M:\n" +
				"	output: result boolean (1..1)\n" +
				"	set result:\n" + 
				"		Person { name: \"Name\", age: empty } -> name only exists\n";


		RosettaModel model = modelHelper.parseRosettaWithNoErrors(code);
		RosettaOnlyExistsExpressionImpl onlyExistsExpression = ((RosettaOnlyExistsExpressionImpl) ((FunctionImpl) 
				model.getElements().get(1)).getOperations().get(0).getExpression());
		
		RosettaInterpreterTypedValue result = (RosettaInterpreterTypedValue) interpreter.interp(onlyExistsExpression);
		
		
		assertNotNull(result);
		// Add "result" (which is the created object) into the environment
//		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment(new HashMap<>());
//		env.addValue("person", result);
//		RosettaExpression expr = parser.parseExpression("person -> name only-exists",
//				List.of("person Person (1..1)"));
		
//		RosettaInterpreterValue val = interpreter.interp(expr);
//		assertTrue(val instanceof RosettaInterpreterBooleanValue);
//		RosettaInterpreterNumberValue castedVal = (RosettaInterpreterNumberValue)val;
//		assertEquals(expected, castedVal);
//		assertEquals("Person", result.getName());
//		assertEquals("name", result.getAttributes().get(0).getName());
//		assertEquals("F", ((RosettaInterpreterStringValue) result.getAttributes().get(0).getValue())
//				.getValue());
//		System.out.println(result.getAttributes().get(1).getValue());
	}
}
