package com.regnosys.rosetta.interpreternew;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterLogicalOperationsTest {
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	private ExpressionFactory eFactory;
	
	@BeforeEach
	public void setup() {
		eFactory = ExpressionFactoryImpl.init();
		
	}
	
	// ------------- Helper Methods -------------
	private RosettaBooleanLiteral createBooleanLiteral(boolean value) {
        RosettaBooleanLiteral literal = eFactory.createRosettaBooleanLiteral();
        literal.setValue(value);
        return literal;
    }

    private LogicalOperation createLogicalOperation(String operator, RosettaExpression left, RosettaExpression right) {
        LogicalOperation operation = eFactory.createLogicalOperation();
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

    private void compareErrors(List<RosettaInterpreterError> expected, EList<RosettaInterpreterBaseError> errors) {
		assertEquals(expected.size(), errors.size());
		for(int i = 0; i < expected.size(); i++) {
			RosettaInterpreterError newError = (RosettaInterpreterError) errors.get(i);
			System.out.println(newError.getError());
			assertEquals(expected.get(i).getError(), newError.getError());
		}
	}
    
    // ------------- Actual Tests -------------
    @Test
    public void logicalAndInterpTestFalse() {
        RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
        RosettaBooleanLiteral falseLiteral = createBooleanLiteral(false);
        LogicalOperation expr = createLogicalOperation("and", trueLiteral, falseLiteral);

        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, false);
    }

    @Test
    public void logicalAndInterpTestTrue() {
        RosettaBooleanLiteral trueLiteral1 = createBooleanLiteral(true);
        RosettaBooleanLiteral trueLiteral2 = createBooleanLiteral(true);
        LogicalOperation expr = createLogicalOperation("and", trueLiteral1, trueLiteral2);

        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, true);
    }

    @Test
    public void logicalOrInterpTestTrue() {
        RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
        RosettaBooleanLiteral falseLiteral = createBooleanLiteral(false);
        LogicalOperation expr = createLogicalOperation("or", trueLiteral, falseLiteral);

        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, true);
    }

    @Test
    public void logicalOrInterpTestFalse() {
        RosettaBooleanLiteral falseLiteral1 = createBooleanLiteral(false);
        RosettaBooleanLiteral falseLiteral2 = createBooleanLiteral(false);
        LogicalOperation expr = createLogicalOperation("or", falseLiteral1, falseLiteral2);

        RosettaInterpreterValue result = interpreter.interp(expr);
        assertBooleanResult(result, false);
    }

    @Test
    public void nestedBooleansLogicalTest() {
        RosettaBooleanLiteral falseLiteral1 = createBooleanLiteral(false);
        RosettaBooleanLiteral falseLiteral2 = createBooleanLiteral(false);
        RosettaBooleanLiteral trueLiteral1 = createBooleanLiteral(true);
        RosettaBooleanLiteral trueLiteral2 = createBooleanLiteral(true);

        LogicalOperation expr1 = createLogicalOperation("and", falseLiteral1, trueLiteral1);
        LogicalOperation expr2 = createLogicalOperation("and", falseLiteral2, trueLiteral2);
        LogicalOperation nestedExpr = createLogicalOperation("or", expr1, expr2);

        RosettaInterpreterValue result = interpreter.interp(nestedExpr);
        assertBooleanResult(result, false);
    }
    
    @Test
    public void wrongOperatorTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	expected.add(new RosettaInterpreterError("Logical Operation: Wrong operator - try 'and' / 'or'"));
    	
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
    	expected.add(new RosettaInterpreterError("Logical Operation: Leftside is not of type Boolean"));
    	
    	RosettaStringLiteral stringLiteral = eFactory.createRosettaStringLiteral();
    	RosettaBooleanLiteral falseLiteral = createBooleanLiteral(false);
    	LogicalOperation expr = createLogicalOperation("and", stringLiteral, falseLiteral);
    	
    	RosettaInterpreterValue result = interpreter.interp(expr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
    
    @Test
    public void errorsOnBothSidesTest() {
    	List<RosettaInterpreterError> expected = new ArrayList<RosettaInterpreterError>();
    	// The order of these might be wrong, I am not sure if they which order they get added in, 
    	// but I assume they get added from the inside, as they get propagated to the out-most expression
    	expected.add(new RosettaInterpreterError("Logical Operation: Rightside is not of type Boolean"));
    	expected.add(new RosettaInterpreterError("Logical Operation: Rightside is an error value"));
    	expected.add(new RosettaInterpreterError("Logical Operation: Leftside is not of type Boolean"));
    	
    	RosettaIntLiteral intLiteral = eFactory.createRosettaIntLiteral();
    	RosettaBooleanLiteral trueLiteral = createBooleanLiteral(true);
    	LogicalOperation expr = createLogicalOperation("or", trueLiteral, intLiteral);
    	
    	RosettaStringLiteral stringLiteral = eFactory.createRosettaStringLiteral();
    	LogicalOperation nestedExpr = createLogicalOperation("and", stringLiteral, expr);
    	
    	RosettaInterpreterValue result = interpreter.interp(nestedExpr);
    	assertTrue(result instanceof RosettaInterpreterErrorValue);
    	RosettaInterpreterErrorValue castedResult = (RosettaInterpreterErrorValue) result;
    	compareErrors(expected, castedResult.getErrors());
    }
}
