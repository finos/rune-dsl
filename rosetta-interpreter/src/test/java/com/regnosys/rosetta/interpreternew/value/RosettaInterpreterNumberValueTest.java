package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;

public class RosettaInterpreterNumberValueTest {
	
	@Test
	void equalsTest() {
		RosettaInterpreterNumberValue n1 = new RosettaInterpreterNumberValue(1);
		RosettaInterpreterNumberValue n2 = new RosettaInterpreterNumberValue(2);
		

		assertTrue(n1.equals(n1));
		assertFalse(n1.equals(n2));
		assertFalse(n1.equals(null));
		assertFalse(n1.equals(3));
	}
	
	@Test
	void streamElementTest() {
		RosettaInterpreterNumberValue n1 = new RosettaInterpreterNumberValue(2);
		List<Object> result = new ArrayList<Object>();
		result.add(new RosettaInterpreterNumberValue(BigDecimal.valueOf(2)).getValue());
		
		assertEquals(result, n1.toElementStream().collect(Collectors.toList()));
	}

}
