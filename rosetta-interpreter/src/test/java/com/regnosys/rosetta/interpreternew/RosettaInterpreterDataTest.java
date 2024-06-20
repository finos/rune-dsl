package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterDataTest {

	@Inject
	RosettaInterpreterNew interpreter;
	
	@Inject 
	ModelHelper modelHelper;
	
	RosettaInterpreterListValue empty = new RosettaInterpreterListValue(List.of());
	
    @Test
    public void dataAddedToEnvTest() {
    	RosettaModel model = modelHelper.parseRosettaWithNoErrors("type Person:\r\n"
    			+ "  name string (1..1)\r\n"
    			+ "\r\n"
    			+ "func MyTest:\r\n"
    			+ "	 inputs: p Person (1..1)"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    p -> name");
    	
    	Data dataType = (Data) model.getElements().get(0);
    	RosettaCardinality card = ((Data) model.getElements().get(0)).getAttributes().get(0).getCard();
    	
    	RosettaInterpreterEnvironment expectedEnv = new RosettaInterpreterEnvironment();
    	expectedEnv.addValue("Person", new RosettaInterpreterTypedValue("Person", 
    			List.of(new RosettaInterpreterTypedFeatureValue("name", empty, card))));
    	RosettaInterpreterEnvironment actualEnv = new RosettaInterpreterEnvironment();
    	
    	RosettaInterpreterEnvironment env = (RosettaInterpreterEnvironment) interpreter.interp(dataType, actualEnv);
        assertEquals((RosettaInterpreterEnvironment) env, (RosettaInterpreterEnvironment) expectedEnv);
    }
    
    @Test
    public void dataRefTest() {
    	RosettaModel model = modelHelper.parseRosettaWithNoErrors("type Person:\r\n"
    			+ "  name string (1..1)\r\n"
    			+ "\r\n"
    			+ "func MyTest:\r\n"
    			+ "	 inputs: p Person (1..1)"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    p -> name");
    	
    	RosettaExpression refCall = ((FunctionImpl) model.getElements().get(1)).getOperations().get(0).getExpression();
    	RosettaCardinality card = ((Data) model.getElements().get(0)).getAttributes().get(0).getCard();
    	
    	RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment();
    	env.addValue("p", new RosettaInterpreterTypedValue("Person", 
    			List.of(new RosettaInterpreterTypedFeatureValue("name", empty, card))));
    	
    	RosettaInterpreterValue value = interpreter.interp(refCall, env);
        assertEquals(value, empty);
    }
    
    @Test
    public void dataAddedToEnvSupertypeTest() {
    	RosettaModel model = modelHelper.parseRosettaWithNoErrors("type Person:\r\n"
    			+ "  name string (1..1)\r\n"
    			+ "type Age extends Person:\r\n"
    			+ "  age number (1..1)"
    			+ "\r\n"
    			+ "func MyTest:\r\n"
    			+ "	 inputs: p Age (1..1)"
    			+ "  output: result string (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    p -> name");
    	
    	Data dataType = (Data) model.getElements().get(1);
    	RosettaCardinality card = ((Data) model.getElements().get(1)).getAttributes().get(0).getCard();
    	
    	RosettaInterpreterEnvironment expectedEnv = new RosettaInterpreterEnvironment();
    	expectedEnv.addValue("Age", new RosettaInterpreterTypedValue("Person", "Age", 
    			List.of(new RosettaInterpreterTypedFeatureValue("age", empty, card))));
    	RosettaInterpreterEnvironment actualEnv = new RosettaInterpreterEnvironment();
    	
    	RosettaInterpreterEnvironment env = (RosettaInterpreterEnvironment) interpreter.interp(dataType, actualEnv);
        assertEquals((RosettaInterpreterEnvironment) env, (RosettaInterpreterEnvironment) expectedEnv);
    }
}
