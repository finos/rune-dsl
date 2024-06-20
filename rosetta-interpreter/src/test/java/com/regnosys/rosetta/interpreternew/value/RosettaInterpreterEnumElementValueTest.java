package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;

public class RosettaInterpreterEnumElementValueTest {
	
	RosettaInterpreterEnumElementValue attribute = 
			new RosettaInterpreterEnumElementValue("a", "b");
	RosettaInterpreterEnumElementValue attribute2 = 
			new RosettaInterpreterEnumElementValue("a", "b");
	RosettaInterpreterEnumElementValue attributeFirst = 
			new RosettaInterpreterEnumElementValue("a", "c");
	RosettaInterpreterEnumElementValue attributeSecond = 
			new RosettaInterpreterEnumElementValue("c", "b");
	
	@Test
	void hashTest() {
		assertEquals(attribute.hashCode(), attribute2.hashCode());
		assertNotEquals(attribute.hashCode(), attributeFirst.hashCode());
		assertNotEquals(attribute.hashCode(), attributeSecond.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(attribute.equals(attribute));
		assertTrue(attribute.equals(attribute2));
		assertFalse(attribute.equals(attributeFirst));
		assertFalse(attribute.equals(attributeSecond));
		assertFalse(attribute.equals(null));
		assertFalse(attribute.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(Arrays.asList("b"), 
				attribute.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterEnumElementValue> stream = 
				Stream.of(attribute).collect(Collectors.toList());
		assertEquals(stream, attribute.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterEnumElementValue [value = "
				+ "b]", attribute.toString());
	}
}
