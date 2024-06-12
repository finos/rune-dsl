package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;

public class RosettaInterpreterDateValueTest {

	RosettaInterpreterNumberValue day = new RosettaInterpreterNumberValue(5);
	RosettaInterpreterNumberValue month = new RosettaInterpreterNumberValue(7);
	RosettaInterpreterNumberValue year = new RosettaInterpreterNumberValue(2024);
	RosettaInterpreterNumberValue day2 = new RosettaInterpreterNumberValue(2);
	RosettaInterpreterNumberValue month2 = new RosettaInterpreterNumberValue(1);
	RosettaInterpreterNumberValue year2 = new RosettaInterpreterNumberValue(2020);
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(day, month, year);
	RosettaInterpreterDateValue date2 = new RosettaInterpreterDateValue(day, month, year);
	RosettaInterpreterDateValue dateDay = new RosettaInterpreterDateValue(day2, month, year);
	RosettaInterpreterDateValue dateMonth = new RosettaInterpreterDateValue(day, month2, year);
	RosettaInterpreterDateValue dateYear = new RosettaInterpreterDateValue(day, month, year2);
	
	@Test
	void hashTest() {
		assertEquals(date.hashCode(), date2.hashCode());
		assertNotEquals(date.hashCode(), dateDay.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(date.equals(date));
		assertTrue(date.equals(date2));
		assertFalse(date.equals(dateDay));
		assertFalse(date.equals(dateMonth));
		assertFalse(date.equals(dateYear));
		assertFalse(date.equals(null));
		assertFalse(date.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of(day, month, year), date.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterDateValue> stream = Stream.of(date).collect(Collectors.toList());
		assertEquals(stream, date.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterDateValue [day=RosettaInterpreterNumberValue [5], "
				+ "month=RosettaInterpreterNumberValue [7], "
				+ "year=RosettaInterpreterNumberValue [2024]]", date.toString());
	}
}
