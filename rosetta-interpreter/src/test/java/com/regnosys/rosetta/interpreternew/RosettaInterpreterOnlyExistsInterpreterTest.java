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
	
	private RosettaModel personModel;
	private RosettaModel fooModel;
	
	@BeforeEach
	public void setup() {
		expFactory = ExpressionFactoryImpl.init();
		personModel = modelHelper.parseRosettaWithNoIssues(
				"type Person:\n" + 
				        "   name string (0..1)\n" +
				        "   height int (0..1)"
					);
		
		fooModel = modelHelper.parseRosettaWithNoIssues(
				"type Foo:\n" +
		        "    bar Bar (0..*)\n" +
		        "    baz Baz (0..1)\n" +
		        "\n" +
		        "type Bar:\n" +
		        "    before number (0..1)\n" +
		        "    after number (0..1)\n" +
		        "    another number (0..1)\n" +
		        "\n" +
		        "type Baz:\n" +
		        "    bazValue number (0..1)\n" +
		        "    other number (0..1)\n"
			        );
	}
	
	@Test
	public void onlyExistsTrueTest() {
		String onlyExistsStr = "person -> name only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(personModel), List.of("person Person (1..1)"));
		
		// Creating the environment (for the interpreter) that contains an instance of Person
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors("type Person: name string (0..1) height number (0..1)"
				+ "func M: output: result Person (1..1) set result: Person { name: \"Simon\", height: empty}");
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("person", dataTypeInstance);

		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertTrue(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsFalseTest() {
		String onlyExistsStr = "person -> name only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(personModel), List.of("person Person (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors("type Person: name string (0..1) height number (0..1)"
				+ "func M: output: result Person (1..1) set result: Person { name: \"Simon\", height: 180}");
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("person", dataTypeInstance);

		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertFalse(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsMultipleFeaturesTrueTest() {	
		String onlyExistsStr = "(person -> name, person -> height) only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(personModel), List.of("person Person (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors("type Person: name string (0..1) height number (0..1)"
				+ "func M: output: result Person (1..1) set result: Person { name: \"Simon\", height: 180}");
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("person", dataTypeInstance);
	
		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertTrue(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsMultipleFeaturesFalseTest() {	
		String onlyExistsStr = "(person -> name, person -> height) only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(personModel), List.of("person Person (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors("type Person: name string (0..1) height number (0..1)"
				+ "func M: output: result Person (1..1) set result: Person { name: \"Simon\", height: empty}");
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(1)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("person", dataTypeInstance);
	
		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertFalse(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsComplexTrueTest() {
		String onlyExistsStr = "foo -> bar -> before only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(fooModel), List.of("foo Foo (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors(
		        "type Foo: bar Bar (0..*) baz Baz (0..1)" +
		        "type Bar: before number (0..1) after number (0..1) another number (0..1)" +
		        "type Baz: bazValue number (0..1) other number (0..1)" +
		        "func M: output: result Foo (1..1) set result: Foo { bar: Bar { before: 10, after: empty, another: empty },"
		        + " baz: Baz { bazValue: 1, other: empty } }"
		    );
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(3)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("foo", dataTypeInstance);
		
		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertTrue(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsComplexMultipleFeaturesTrueTest() {
		String onlyExistsStr = "(foo -> bar -> before, foo -> bar -> another) only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(fooModel), List.of("foo Foo (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors(
		        "type Foo: bar Bar (0..*) baz Baz (0..1)" +
		        "type Bar: before number (0..1) after number (0..1) another number (0..1)" +
		        "type Baz: bazValue number (0..1) other number (0..1)" +
		        "func M: output: result Foo (1..1) set result: Foo { bar: Bar { before: 10, after: empty, another: 50 },"
		        + " baz: Baz { bazValue: 1, other: empty } }"
		    );
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(3)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("foo", dataTypeInstance);
		
		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertTrue(castedVal.getValue());
	}
	
	@Test
	public void onlyExistsComplexMultipleFeaturesFalseTest() {
		String onlyExistsStr = "(foo -> bar -> before, foo -> bar -> another) only exists";
		RosettaExpression onlyExistsExpression = 
				parser.parseExpression(onlyExistsStr, List.of(fooModel), List.of("foo Foo (1..1)"));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
		RosettaModel constructorModel = modelHelper.parseRosettaWithNoErrors(
		        "type Foo: bar Bar (0..*) baz Baz (0..1)" +
		        "type Bar: before number (0..1) after number (0..1) another number (0..1)" +
		        "type Baz: bazValue number (0..1) other number (0..1)" +
		        "func M: output: result Foo (1..1) set result: Foo { bar: Bar { before: 10, after: 25, another: 50 },"
		        + " baz: Baz { bazValue: 1, other: empty } }"
		    );
		RosettaConstructorExpressionImpl constructor = ((RosettaConstructorExpressionImpl) ((
				FunctionImpl) constructorModel.getElements().get(3)).getOperations().get(0).getExpression());
		RosettaInterpreterTypedValue dataTypeInstance = (RosettaInterpreterTypedValue) interpreter.interp(constructor);
		env.addValue("foo", dataTypeInstance);
		
		RosettaInterpreterValue val = interpreter.interp(onlyExistsExpression, env);
		System.out.println(val);
		assertTrue(val instanceof RosettaInterpreterBooleanValue);
		RosettaInterpreterBooleanValue castedVal = (RosettaInterpreterBooleanValue)val;
		assertFalse(castedVal.getValue());
	}
}
