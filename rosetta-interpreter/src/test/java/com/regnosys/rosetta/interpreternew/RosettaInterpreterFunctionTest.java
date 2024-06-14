package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterFunctionValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaSymbolReferenceImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterFunctionTest {
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Inject 
	ModelHelper mh;
	
	RosettaInterpreterNumberValue day = new RosettaInterpreterNumberValue(5);
	RosettaInterpreterNumberValue month = new RosettaInterpreterNumberValue(7);
	RosettaInterpreterNumberValue year = new RosettaInterpreterNumberValue(2024);
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(day, month, year);
	
	RosettaInterpreterNumberValue hours = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
	RosettaInterpreterNumberValue minutes = new RosettaInterpreterNumberValue(BigDecimal.valueOf(30));
	RosettaInterpreterNumberValue seconds = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	
//	private ExpressionFactory exFactory;
//	
//	@BeforeEach
//	public void setup() {
//		exFactory = ExpressionFactoryImpl.init();
//		
//	}
	
    @Test
    public void funcAddsToEnvironmentTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func MyTest:\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  set result:\r\n"
    			+ "    3\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	assertEquals(env.findValue(function.getName()),
    			new RosettaInterpreterFunctionValue(function));
    }
    
    @Test
    public void inputCardinalityTooHigh() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add([1.0, 2.0])\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" has cardinality higher than the limit 1"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void inputCardinalityTooLow() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (2..3)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" has cardinality lower than the limit 2"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void numberMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(\"Hi\")\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a number, but received a " 
    					+ (new RosettaInterpreterStringValue("")).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void stringMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a string (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a string, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void intMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(\"Hi\")\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a number, but received a " 
    					+ (new RosettaInterpreterStringValue("")).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void booleanMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a boolean (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + 2.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a boolean, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void dateMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a date (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a date, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void dateTimeMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a dateTime (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a dateTime, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void zonedDateTimeMismatch() {
    	//Had to make this parse without errors, otherwise it would've checked the cardinality mismatch on its own
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a zonedDateTime (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"a\" requires a zonedDateTime, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcSimpleSetTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterNumberValue expected = new RosettaInterpreterNumberValue(BigDecimal.valueOf(3));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcSimpleSetTestWithEnum() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"enum RoundingModeEnum: \r\n"
    			+ "    Down \r\n"
    			+ "    Up \r\n"
    			+ "func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(1);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(2)).getOperations().get(0).getExpression();
    	Map<String, RosettaInterpreterValue> nv = new HashMap<String, RosettaInterpreterValue>();
    	RosettaInterpreterEnvironment env2 = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterEnvironment env1 = 
    			(RosettaInterpreterEnvironment) interpreter.interp((RosettaEnumeration) model
    					.getElements().get(0));
    	nv.putAll(env2.getEnvironment());
    	nv.putAll(env1.getEnvironment());
    	RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment(nv);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterNumberValue expected = new RosettaInterpreterNumberValue(BigDecimal.valueOf(3));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcSimpleBooleanSetTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a boolean (1..*)\r\n"
    			+ "  output: result boolean (1..*)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result boolean (1..*)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(True)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterBooleanValue(true)));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcSimpleDateSetTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a date (1..*)\r\n"
    			+ "  output: result date (1..*)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result date (1..*)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(date { day: 1, month: 1, year: 1 })\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterNumberValue n = new RosettaInterpreterNumberValue(BigDecimal.valueOf(1));
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterDateValue(n, n, n)));
    	assertEquals(expected, res);
    }

    @Test
    public void funcPreConditionTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  condition Alarger:\r\n"
    			+ "    a > b\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(3.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterNumberValue expected = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcPostConditionTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a string (1..1)\r\n"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "  post-condition TrueString:\r\n"
    			+ "    result = \"True\"\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(\"True\")\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterStringValue expected = new RosettaInterpreterStringValue("True");
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcPreConditionTestError() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  condition Alarger:\r\n"
    			+ "    a > b\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("Condition \"Alarger\" does not hold for this function call"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcBadPreConditionTest() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  condition Alarger:\r\n"
    			+ "    a any > [b]\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(3.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("cannot use \"ANY\" keyword "
						+ "to compare two elements"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcBadPostConditionTest() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "  post-condition Alarger:\r\n"
    			+ "    [a] any > [b]\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(3.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("cannot compare two lists"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcOperationError() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + True\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("Arithmetic Operation: Rightside"
    					+ " is not of type Number/String"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcArgumentInterpretError() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add((1.0 + True))\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("Arithmetic Operation: Rightside"
    					+ " is not of type Number/String"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcPostConditionTestError() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a string (1..1)\r\n"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    a\r\n"
    			+ "  post-condition FalseString:\r\n"
    			+ "    result = \"False\"\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(\"True\")\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("Condition \"FalseString\" does not hold for this function call"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void funcOutputWrongTypeError() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a string (1..1)\r\n"
    			+ "  output: result string (0..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    3.0\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(\"True\")\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("The attribute \"result\" requires a string, but received a " 
    					+ (new RosettaInterpreterNumberValue(BigDecimal.valueOf(0))).getClass()));
    	assertEquals(expected, res);
    }
    
    @Test
    public void aliasTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "	 alias bee: b "
    			+ "  set result:\r\n"
    			+ "    a + bee\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(2.0, 1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(3))));
    	assertEquals(expected, res);
    }
    
    @Test
    public void aliasErrorTest() {
    	RosettaModel model = mh.parseRosetta(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "	 alias bee: b + True\r\n"
    			+ "  set result:\r\n"
    			+ "    a + bee\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(2.0, 1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterErrorValue expected = new RosettaInterpreterErrorValue(
    			new RosettaInterpreterError("Arithmetic Operation: Rightside"
    					+ " is not of type Number/String"));
    	assertEquals(expected, res);
    }
    
    @Test
    public void complexFunctionText() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  condition Alarger:\r\n"
    			+ "    a > b\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "  add result: 1.0\r\n"
    			+ "	 add result: 2.0\r\n"
    			+ "	 post-condition Countt:\r\n"
    			+ "	   result count = 3\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result number (1..10)\r\n"
    			+ "  set result:\r\n"
    			+ "    Add(2.0, 1.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(3)),
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)),
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
    	assertEquals(expected, res);
    }
    
}