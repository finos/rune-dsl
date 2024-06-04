package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterLogicalOperationsTest {
	
	@Inject
	private ExpressionParser parser;
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	private ExpressionFactory exFactory;
	
	@BeforeEach
	public void setup() {
		exFactory = ExpressionFactoryImpl.init();
		
	}
	
	// ------------- Helper Methods -------------
	private RosettaBooleanLiteral createBooleanLiteral(boolean value) {
        RosettaBooleanLiteral literal = exFactory.createRosettaBooleanLiteral();
        literal.setValue(value);
        return literal;
    }

    private LogicalOperation createLogicalOperation(
    		String operator, RosettaExpression left, RosettaExpression right) {
        LogicalOperation operation = exFactory.createLogicalOperation();
        operation.setOperator(operator);
        operation.setLeft(left);
        operation.setRight(right);
        return operation;
    }

    private void assertBooleanResult(RosettaInterpreterValue result, boolean expected) {
        assertTrue(result instanceof RosettaInterpreterBooleanValue);
        RosettaInterpreterBooleanValue boolResult = (RosettaInterpreterBooleanValue) result;
        assertEquals(expected, boolResult.getValue());
    }

    private void compareErrors(
    		List<RosettaInterpreterError> expected, 
    		EList<RosettaInterpreterBaseError> errors) {
		assertEquals(expected.size(), errors.size());
		for (int i = 0; i < expected.size(); i++) {
			RosettaInterpreterError newError = (RosettaInterpreterError) errors.get(i);
			assertEquals(expected.get(i).getError(), newError.getError());
		}
	}
    
    // ------------- Actual Tests -------------
    @Test
    public void logicalAndInterpTestFalse() {
    	RosettaExpression expr = parser.parseExpression("True and False");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, false);
    }

    @Test
    public void logicalAndInterpTestTrue() {
        RosettaExpression expr = parser.parseExpression("True and True");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, true);
    }
    
    @Test
    public void logicalOrInterpTestTrue() {
    	RosettaExpression expr = parser.parseExpression("True or False");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, true);
    }
    
    @Test
    public void logicalOrInterpTestTrue2() {
        RosettaExpression expr = parser.parseExpression("False or True");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, true);
    }

    @Test
    public void logicalOrInterpTestFalse() {
        RosettaExpression expr = parser.parseExpression("False or False");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, false);
    }   

    @Test
    public void nestedBooleansLogicalTest() {
        RosettaExpression expr = parser.parseExpression("(False and True) or (False and True)");
        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, false);
    }
    
    @Test
    public void wrongOperatorTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	expected.add(new RosettaInterpreterError(
    			"Logical Operation: Wrong operator - only 'and' / 'or' supported"));
    	
    	RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
    	RosettaBooleanLiteral falseLiteral = createBooleanLiteral(false);
    	LogicalOperation expr = createLogicalOperation("xor", trueLiteral, falseLiteral);
 
    	RosettaInterpreterValue result = interpreter.interp(expr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
    
    @Test
    public void notBooleanValueTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	expected.add(new RosettaInterpreterError(
    			"Logical Operation: Leftside is not of type Boolean"));
    	
    	RosettaExpression expr = parser.parseExpression("1 and False");
    	RosettaInterpreterValue result = interpreter.interp(expr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
    
    @Test
    public void errorOnRightSideTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	// This is the case: (False and (True or 1))
    	expected.add(new RosettaInterpreterError(
    			"Logical Operation: Rightside is not of type Boolean"));
    	
    	RosettaIntLiteral intLiteral = exFactory.createRosettaIntLiteral();
    	intLiteral.setValue(BigInteger.valueOf(1));
    	RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
    	LogicalOperation expr = createLogicalOperation("or", trueLiteral, intLiteral);
    	
    	RosettaBooleanLiteral falseLiteral = createBooleanLiteral(false);
    	LogicalOperation nestedExpr = createLogicalOperation("and", falseLiteral, expr);
    	
    	RosettaInterpreterValue result = interpreter.interp(nestedExpr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
    
    @Test
    public void errorsOnBothSidesTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	// This is the case: ("string" and (True or 1))
    	expected.add(new RosettaInterpreterError(
    			"Logical Operation: Leftside is not of type Boolean"));
    	expected.add(new RosettaInterpreterError(
    			"Logical Operation: Rightside is not of type Boolean"));
    	
    	RosettaIntLiteral intLiteral = exFactory.createRosettaIntLiteral();
    	intLiteral.setValue(BigInteger.valueOf(1));
    	RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
    	LogicalOperation expr = createLogicalOperation("or", trueLiteral, intLiteral);
    	
    	RosettaStringLiteral stringLiteral = exFactory.createRosettaStringLiteral();
    	stringLiteral.setValue("string");
    	LogicalOperation nestedExpr = createLogicalOperation("and", stringLiteral, expr);
    	
    	RosettaInterpreterValue result = interpreter.interp(nestedExpr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
}
