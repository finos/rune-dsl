package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
//import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
//import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterEnumTest {
	
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
    public void enumAddsToEnvironmentTest() {
    	RosettaModel model = mh.parseRosettaWithNoErrors("enum Foo:\r\n"
    			+ "  VALU_E1 displayName \"VALU.E1\"\r\n"
    			+ "  VALUE2\r\n"
    			+ "\r\n"
    			+ "func MyTest:\r\n"
    			+ "  output: result Foo (1..1)\r\n"
    			+ "  set result:\r\n"
    			+ "    Foo -> VALU_E1");
    	RosettaInterpreterEnvironment expectedEnv = 
    			new RosettaInterpreterEnvironment();
    	expectedEnv.addValue("Foo", 
    			new RosettaInterpreterEnumValue("Foo", 
    					List.of(new RosettaInterpreterEnumElementValue("Foo", "VALU_E1"),
    							new RosettaInterpreterEnumElementValue("Foo", "VALUE2"))));
    	RosettaInterpreterEnvironment actualEnv = 
    			new RosettaInterpreterEnvironment();
    	RosettaEnumeration enumeration = (RosettaEnumeration) model.getElements().get(0);
    	RosettaInterpreterEnvironment env = (RosettaInterpreterEnvironment)
    			interpreter.interp(enumeration, actualEnv);
        assertEquals((RosettaInterpreterEnvironment) env,
        		(RosettaInterpreterEnvironment) expectedEnv);
    }
    
//    @Test
//    public void enumRefTest() {
//	RosettaModel model = mh.parseRosettaWithNoErrors("enum Foo:\r\n"
//			+ "  VALU_E1 displayName \"VALU.E1\"\r\n"
//			+ "  VALUE2\r\n"
//			+ "\r\n"
//			+ "func MyTest:\r\n"
//			+ "  output: result Foo (1..1)\r\n"
//			+ "  set result:\r\n"
//			+ "    Foo -> VALU_E1");
//	RosettaInterpreterEnvironment expectedEnv = 
//			new RosettaInterpreterEnvironment();
//	expectedEnv.addValue("Foo", 
//			new RosettaInterpreterEnumValue("Foo", 
//					List.of(new RosettaInterpreterEnumElementValue("Foo", "VALU_E1"),
//							new RosettaInterpreterEnumElementValue("Foo", "VALUE2"))));
//	RosettaInterpreterEnvironment actualEnv = 
//			new RosettaInterpreterEnvironment();
//	RosettaEnumeration enumeration = (RosettaEnumeration) model.getElements().get(0);
//	RosettaExpression refCall = ((FunctionImpl) model.getElements().get(1)).getOperations()
//			.get(0).getExpression();
//	RosettaInterpreterValue env = (RosettaInterpreterEnvironment)
//			interpreter.interp(refCall, actualEnv);
//    assertEquals((RosettaInterpreterEnvironment) env,
//    		(RosettaInterpreterEnvironment) expectedEnv);
//    }
    
}