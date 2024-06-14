package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;

public class RosettaInterpreterTypedFeatureValueTest {
	
	RosettaInterpreterStringValue value = new RosettaInterpreterStringValue("a");
	RosettaInterpreterStringValue value2 = new RosettaInterpreterStringValue("b");
	
	RosettaInterpreterTypedFeatureValue attribute = new RosettaInterpreterTypedFeatureValue("name", value, null);
	RosettaInterpreterTypedFeatureValue attribute2 = new RosettaInterpreterTypedFeatureValue("name", value, null);
	RosettaInterpreterTypedFeatureValue attributeName = 
			new RosettaInterpreterTypedFeatureValue("nickname", value, null);
	RosettaInterpreterTypedFeatureValue attributeValue = 
			new RosettaInterpreterTypedFeatureValue("name", value2, null);
	
	@Test
	void getCardTest() {
		assertNull(attribute.getCard());
	}
	
	@Test
	void hashTest() {
		assertEquals(attribute.hashCode(), attribute2.hashCode());
		assertNotEquals(attribute.hashCode(), attributeName.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(attribute.equals(attribute));
		assertTrue(attribute.equals(attribute2));
		assertFalse(attribute.equals(attributeName));
		assertFalse(attribute.equals(attributeValue));
		assertFalse(attribute.equals(null));
		assertFalse(attribute.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(Arrays.asList("name", value, null), 
				attribute.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterTypedFeatureValue> stream = Stream.of(attribute).collect(Collectors.toList());
		assertEquals(stream, attribute.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterTypedFeatureValue [name=name, value=RosettaInterpreterStringValue "
				+ "[a], card=null]", attribute.toString());
	}
}
