package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;

public class RosettaInterpreterEnumValueTest {

	RosettaInterpreterStringValue value = new RosettaInterpreterStringValue("a");	

	RosettaInterpreterEnumElementValue attribute = new RosettaInterpreterEnumElementValue("E", "Value1");
	RosettaInterpreterEnumElementValue attribute2 = new RosettaInterpreterEnumElementValue("E", "Value1");
	RosettaInterpreterEnumElementValue attributeName = 
			new RosettaInterpreterEnumElementValue("E", "Value2");
	
	RosettaInterpreterEnumValue data = new RosettaInterpreterEnumValue("E", List.of(attribute));
	RosettaInterpreterEnumValue data2 = new RosettaInterpreterEnumValue("E", List.of(attribute2));

	RosettaInterpreterEnumValue dataName = 
			new RosettaInterpreterEnumValue("F", List.of(attribute));
	RosettaInterpreterEnumValue dataAttribute = 
			new RosettaInterpreterEnumValue("E", List.of(attributeName));
	
	@Test
	void hashTest() {
		assertEquals(data.hashCode(), data2.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(data.equals(data));
		assertTrue(data.equals(data2));
		assertFalse(data.equals(dataName));
		assertFalse(data.equals(dataAttribute));
		assertFalse(data.equals(null));
		assertFalse(data.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of(attribute), 
				data.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterEnumValue> stream = Stream.of(data).collect(Collectors.toList());
		assertEquals(stream, data.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterListValue [name = E, values = ["
				+ "RosettaInterpreterEnumElementValue [value = Value1]]]", data.toString());
	}
}
