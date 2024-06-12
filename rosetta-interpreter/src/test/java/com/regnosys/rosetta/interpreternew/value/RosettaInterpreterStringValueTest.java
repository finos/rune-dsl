package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;

public class RosettaInterpreterStringValueTest {

	@Test
	void hashTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("string1");
		RosettaInterpreterStringValue s2 = new RosettaInterpreterStringValue("string1");
		RosettaInterpreterStringValue s3 = new RosettaInterpreterStringValue("string2");
		
		assertEquals(s1.hashCode(), s2.hashCode());
		assertNotEquals(s1.hashCode(), s3.hashCode());
	}
	
	@Test
	void getValueTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("string1");
		
		assertEquals("string1", s1.getValue());
	}
	
	@Test
	void equalsTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("string1");
		RosettaInterpreterStringValue s2 = new RosettaInterpreterStringValue("string1");
		
		assertTrue(s1.equals(s2));
		assertTrue(s1.equals(s1));
	}
	
	@Test
	void equalsBadWeatherTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("string1");
		RosettaInterpreterStringValue s2 = new RosettaInterpreterStringValue("string2");
		

		assertFalse(s1.equals(s2));
		assertFalse(s1.equals(null));
		assertFalse(s1.equals("notString"));
	}

	
	@Test
	void compareToTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("a");
		RosettaInterpreterStringValue s2 = new RosettaInterpreterStringValue("a");
		RosettaInterpreterStringValue s3 = new RosettaInterpreterStringValue("c");
		
		assertEquals(0, s1.compareTo(s2));
		assertEquals(-1, s1.compareTo(s3));
		assertEquals(1, s3.compareTo(s2));
	}
	
	@Test
	void streamElementTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("a");
		
		assertEquals(List.of("a"), s1.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("a");
		List<RosettaInterpreterStringValue> stream = Stream.of(s1).collect(Collectors.toList());
		assertEquals(stream, s1.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		RosettaInterpreterStringValue s1 = new RosettaInterpreterStringValue("a"); 
		
		assertEquals("RosettaInterpreterStringValue [a]", s1.toString());
	}
	
}
