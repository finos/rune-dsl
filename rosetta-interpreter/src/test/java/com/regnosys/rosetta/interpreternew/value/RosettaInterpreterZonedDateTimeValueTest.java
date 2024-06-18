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

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterZonedDateTimeValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterDateValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterStringValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;

public class RosettaInterpreterZonedDateTimeValueTest {

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
	
	RosettaInterpreterStringValue zone = new RosettaInterpreterStringValue("CET");
	RosettaInterpreterStringValue zone2 = new RosettaInterpreterStringValue("CEST");
	
	RosettaInterpreterZonedDateTimeValue zonedDateTime = 
			new RosettaInterpreterZonedDateTimeValue(date, time, zone);
	RosettaInterpreterZonedDateTimeValue zonedDateTime2 = 
			new RosettaInterpreterZonedDateTimeValue(date, time, zone);
	RosettaInterpreterZonedDateTimeValue zonedDateTimeDate = 
			new RosettaInterpreterZonedDateTimeValue(dateDay, time, zone);
	RosettaInterpreterZonedDateTimeValue zonedDateTimeTime = 
			new RosettaInterpreterZonedDateTimeValue(date, timeHours, zone);
	RosettaInterpreterZonedDateTimeValue zonedDateTimeZone = 
			new RosettaInterpreterZonedDateTimeValue(date, time, zone2);
	
	@Test
	void hashTest() {
		assertEquals(zonedDateTime.hashCode(), zonedDateTime2.hashCode());
		assertNotEquals(zonedDateTime.hashCode(), zonedDateTimeDate.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(zonedDateTime.equals(zonedDateTime));
		assertTrue(zonedDateTime.equals(zonedDateTime2));
		assertFalse(zonedDateTime.equals(zonedDateTimeDate));
		assertFalse(zonedDateTime.equals(zonedDateTimeTime));
		assertFalse(zonedDateTime.equals(zonedDateTimeZone));
		assertFalse(zonedDateTime.equals(null));
		assertFalse(zonedDateTime.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of(date, time, zone), zonedDateTime.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterZonedDateTimeValue> stream = Stream.of(zonedDateTime)
				.collect(Collectors.toList());
		assertEquals(stream, zonedDateTime.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterZonedDateTimeValue [date=RosettaInterpreterDateValue "
				+ "[day=RosettaInterpreterNumberValue [5], month=RosettaInterpreterNumberValue [7], "
				+ "year=RosettaInterpreterNumberValue [2024]], time=RosettaInterpreterTimeValue "
				+ "[hours=RosettaInterpreterNumberValue [5], minutes=RosettaInterpreterNumberValue "
				+ "[30], seconds=RosettaInterpreterNumberValue [28]], "
				+ "timeZone=RosettaInterpreterStringValue [CET]]", zonedDateTime.toString());
	}
}
