package com.regnosys.rosetta.interpreternew;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterErrorTest {

	@Test
	public void simpleErrorTest() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("e1",null);
		RosettaInterpreterError e2 = new RosettaInterpreterError("e2",null);
		
		RosettaInterpreterErrorValue val1 = new RosettaInterpreterErrorValue();
		RosettaInterpreterErrorValue val2 = new RosettaInterpreterErrorValue();
		
		val1.addError(e1);
		val2.addError(e2);
		
		val1.addAllErrors(val2);
		
		List<RosettaInterpreterError> el = List.of(e1, e2);
		assertEquals(el, val1.getErrors());
	}
	
	@Test
	public void errorMessageExists() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("e1",null);
		RosettaInterpreterErrorValue val1 = new RosettaInterpreterErrorValue(e1);
		assertEquals("e1", val1.getErrors().get(0).getMessage());
	}
}
