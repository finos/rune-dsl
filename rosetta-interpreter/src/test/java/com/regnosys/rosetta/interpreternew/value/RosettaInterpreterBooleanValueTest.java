package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;

public class RosettaInterpreterBooleanValueTest {
	
	@Test
	void equalsGoodWeatherTest() {
		RosettaInterpreterBooleanValue b1 = new RosettaInterpreterBooleanValue(true);
		RosettaInterpreterBooleanValue b2 = new RosettaInterpreterBooleanValue(true);
		
		assertTrue(b1.equals(b1));
		assertTrue(b1.equals(b2));
	}
	
	@Test
	void equalsBadWeatherTest() {
		RosettaInterpreterBooleanValue b1 = new RosettaInterpreterBooleanValue(true);
		RosettaInterpreterBooleanValue b2 = new RosettaInterpreterBooleanValue(false);
		
		assertFalse(b1.equals(b2));
		assertFalse(b1.equals(null));
		assertFalse(b1.equals(true));
	}
	
	@Test
	void streamElementTest() {
		RosettaInterpreterBooleanValue b1 = new RosettaInterpreterBooleanValue(true);
		List<Object> result = new ArrayList<Object>();
		result.add(true);
		
		assertEquals(result, b1.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		RosettaInterpreterBooleanValue b1 = new RosettaInterpreterBooleanValue(true);
		
		assertEquals("RosettaInterpreterBooleanValue [value=true]", b1.toString());
	}

}
