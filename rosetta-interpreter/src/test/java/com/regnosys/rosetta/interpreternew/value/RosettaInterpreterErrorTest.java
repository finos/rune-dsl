package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;

public class RosettaInterpreterErrorTest {

	@Test
	void hashTest() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("error1", null);
		RosettaInterpreterError e2 = new RosettaInterpreterError("error1", null);
		RosettaInterpreterError e3 = new RosettaInterpreterError("error3", null);
		
		assertEquals(e1.hashCode(), e2.hashCode());
		assertNotEquals(e1.hashCode(), e3.hashCode());
	}
	
	@Test
	void equalsGoodWeatherTest() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("error1", null);
		RosettaInterpreterError e2 = new RosettaInterpreterError("error1", null);
		
		assertTrue(e1.equals(e1));
		assertTrue(e1.equals(e2));
	}
	
	@Test
	void equalsBadWeatherTest() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("error1", null);
		RosettaInterpreterError e2 = new RosettaInterpreterError("error2", null);
		
		assertFalse(e1.equals(e2));
		assertFalse(e1.equals(null));
		assertFalse(e1.equals("notAnError"));
	}
	
	@Test
	void toStringTest() {
		RosettaInterpreterError e1 = new RosettaInterpreterError("error1", null);		
		
		assertEquals("error1", e1.toString());
	}
}
