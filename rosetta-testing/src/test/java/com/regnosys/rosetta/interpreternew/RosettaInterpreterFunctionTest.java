package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterFunctionValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaSymbolReferenceImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
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
    public void cardinalityMismatch() {
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
    			new RosettaInterpreterError("Argument a does not correspond with its passed value"));
    	assertEquals(res, expected);
    }
    
    @Test
    public void funcSimpleSetTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors(
    			"func Add:\r\n"
    			+ "  inputs:"
    			+ "		a number (1..1)\r\n"
    			+ "		b int (1..1)\r\n"
    			+ "  output: result number (1..10)\r\n"
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
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(3))));
    	assertEquals(res, expected);
    }
    
    @Test
    public void funcConditionTest() {
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
    			+ "    Add(3.0, 2.0)\r\n");
    	FunctionImpl function = (FunctionImpl) model.getElements().get(0);
    	RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) 
    			((FunctionImpl)model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaInterpreterEnvironment env = 
    			(RosettaInterpreterEnvironment) interpreter.interp(function);
    	RosettaInterpreterValue res = interpreter.interp(ref, env);
    	RosettaInterpreterListValue expected = new RosettaInterpreterListValue(List.of(
    			new RosettaInterpreterNumberValue(BigDecimal.valueOf(5))));
    	assertEquals(res, expected);
    }
    
    @Test
    public void funcConditionTestError() {
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
    			new RosettaInterpreterError("Condition Alarger does not hold for this function call"));
    	assertEquals(res, expected);
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
//    			+ "	 post-condition Countt:\r\n"
//    			+ "	   result count = (a + b)\r\n"
    			+ "  set result:\r\n"
    			+ "    a + b\r\n"
    			+ "  add result: 1.0\r\n"
    			+ "	 add result: 2.0\r\n"
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
    	assertEquals(res, expected);
    }
    
}