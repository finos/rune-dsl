package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;

public class RosettaInterpreterListValueTest {

	@Test
	void hashTest() {
		RosettaInterpreterListValue l1 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		RosettaInterpreterListValue l2 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		RosettaInterpreterListValue l3 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(3))));
		
		assertEquals(l1.hashCode(), l2.hashCode());
		assertNotEquals(l1.hashCode(), l3.hashCode());
		
	}
	
	@Test
	void equalsGoodWeatherTest() {
		RosettaInterpreterListValue l1 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		RosettaInterpreterListValue l2 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		
		assertTrue(l1.equals(l1));
		assertTrue(l1.equals(l2));
		
	}
	
	@Test
	void equalsBadWeatherTest() {
		RosettaInterpreterListValue l1 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		RosettaInterpreterListValue l2 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(3))));
		
		assertFalse(l1.equals(l2));
		assertFalse(l1.equals(null));
		assertFalse(l1.equals("hey"));
	}
	
	@Test
	void toStringTest() {
		RosettaInterpreterListValue l1 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		
		assertEquals("RosettaInterpreterListValue "
				+ "[expressions=[RosettaInterpreterNumberValue [1], "
				+ "RosettaInterpreterNumberValue [2]]]", 
				l1.toString());
	}
	
	@Test
	void streamElementTest() {
		RosettaInterpreterListValue l1 = 
				new RosettaInterpreterListValue(List.of(
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)), 
						new RosettaInterpreterNumberValue(BigDecimal.valueOf(2))));
		
		List<Object> result = new ArrayList<Object>();
		result.add(new RosettaInterpreterNumberValue(BigDecimal.valueOf(1)));
		result.add(new RosettaInterpreterNumberValue(BigDecimal.valueOf(2)));
		
		assertEquals(result, l1.toElementStream().collect(Collectors.toList()));
	}
}
