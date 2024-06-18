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
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;

public class RosettaInterpreterTypedValueTest {

	RosettaInterpreterStringValue value = new RosettaInterpreterStringValue("a");	
	RosettaInterpreterTypedFeatureValue attribute = new RosettaInterpreterTypedFeatureValue("name", value, null);
	RosettaInterpreterTypedFeatureValue attribute2 = new RosettaInterpreterTypedFeatureValue("name", value, null);
	RosettaInterpreterTypedFeatureValue attributeName = 
			new RosettaInterpreterTypedFeatureValue("nickname", value, null);
	
	RosettaInterpreterTypedValue data = new RosettaInterpreterTypedValue("super", "data", List.of(attribute));
	RosettaInterpreterTypedValue data2 = new RosettaInterpreterTypedValue("super", "data", List.of(attribute2));
	RosettaInterpreterTypedValue dataSuper = 
			new RosettaInterpreterTypedValue("supernot", "data", List.of(attribute));
	RosettaInterpreterTypedValue dataName = 
			new RosettaInterpreterTypedValue("super", "datanot", List.of(attribute));
	RosettaInterpreterTypedValue dataAttribute = 
			new RosettaInterpreterTypedValue("super", "data", List.of(attributeName));
	RosettaInterpreterTypedValue dataNotSuper = new RosettaInterpreterTypedValue("data", List.of(attribute));
	
	@Test
	void hasSuperTypeTest() {
		assertTrue(data.hasSuperType());
		assertFalse(dataNotSuper.hasSuperType());
	}
	
	@Test
	void hashTest() {
		assertEquals(data.hashCode(), data2.hashCode());
		assertNotEquals(data.hashCode(), dataName.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(data.equals(data));
		assertTrue(data.equals(data2));
		assertFalse(data.equals(dataSuper));
		assertFalse(data.equals(dataName));
		assertFalse(data.equals(dataAttribute));
		assertFalse(data.equals(null));
		assertFalse(data.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of("super", "data", List.of(attribute)), 
				data.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterTypedValue> stream = Stream.of(data).collect(Collectors.toList());
		assertEquals(stream, data.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterTypedValue [supertype=super, name=data, "
				+ "attributes=[RosettaInterpreterTypedFeatureValue [name=name, "
				+ "value=RosettaInterpreterStringValue [a], card=null]]]", data.toString());
	}
}
