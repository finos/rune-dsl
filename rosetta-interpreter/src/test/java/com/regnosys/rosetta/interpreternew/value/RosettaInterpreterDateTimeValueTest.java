package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;

public class RosettaInterpreterDateTimeValueTest {

	RosettaInterpreterNumberValue day = new RosettaInterpreterNumberValue(5);
	RosettaInterpreterNumberValue month = new RosettaInterpreterNumberValue(7);
	RosettaInterpreterNumberValue year = new RosettaInterpreterNumberValue(2024);
	RosettaInterpreterNumberValue day2 = new RosettaInterpreterNumberValue(2);
	
	RosettaInterpreterDateValue date = new RosettaInterpreterDateValue(day, month, year);
	RosettaInterpreterDateValue dateDay = new RosettaInterpreterDateValue(day2, month, year);
	
	RosettaInterpreterNumberValue hours = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
	RosettaInterpreterNumberValue minutes = new RosettaInterpreterNumberValue(BigDecimal.valueOf(30));
	RosettaInterpreterNumberValue seconds = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	
	RosettaInterpreterNumberValue hours2 = new RosettaInterpreterNumberValue(BigDecimal.valueOf(18));
	RosettaInterpreterTimeValue timeHours = new RosettaInterpreterTimeValue(hours2, minutes, seconds);
	
	RosettaInterpreterDateTimeValue dateTime = new RosettaInterpreterDateTimeValue(date, time);
	RosettaInterpreterDateTimeValue dateTime2 = new RosettaInterpreterDateTimeValue(date, time);
	RosettaInterpreterDateTimeValue dateTimeDate = new RosettaInterpreterDateTimeValue(dateDay, time);
	RosettaInterpreterDateTimeValue dateTimeTime = new RosettaInterpreterDateTimeValue(date, timeHours);
	
	@Test
	void hashTest() {
		assertEquals(dateTime.hashCode(), dateTime2.hashCode());
		assertNotEquals(dateTime.hashCode(), dateTimeDate.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(dateTime.equals(dateTime));
		assertTrue(dateTime.equals(dateTime2));
		assertFalse(dateTime.equals(dateTimeDate));
		assertFalse(dateTime.equals(dateTimeTime));
		assertFalse(dateTime.equals(null));
		assertFalse(dateTime.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of(date, time), dateTime.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterDateTimeValue> stream = Stream.of(dateTime).collect(Collectors.toList());
		assertEquals(stream, dateTime.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterDateTimeValue [date=RosettaInterpreterDateValue "
				+ "[day=RosettaInterpreterNumberValue [5], month=RosettaInterpreterNumberValue [7], "
				+ "year=RosettaInterpreterNumberValue [2024]], time=RosettaInterpreterTimeValue "
				+ "[hours=RosettaInterpreterNumberValue [5], minutes=RosettaInterpreterNumberValue "
				+ "[30], seconds=RosettaInterpreterNumberValue [28]]]", dateTime.toString());
	}
}
