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

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTimeValue;

public class RosettaInterpreterTimeValueTest {

	RosettaInterpreterNumberValue hours = new RosettaInterpreterNumberValue(BigDecimal.valueOf(5));
	RosettaInterpreterNumberValue minutes = new RosettaInterpreterNumberValue(BigDecimal.valueOf(30));
	RosettaInterpreterNumberValue seconds = new RosettaInterpreterNumberValue(BigDecimal.valueOf(28));
	RosettaInterpreterTimeValue time = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	RosettaInterpreterTimeValue time2 = new RosettaInterpreterTimeValue(hours, minutes, seconds);
	
	RosettaInterpreterNumberValue hoursError = new RosettaInterpreterNumberValue(BigDecimal.valueOf(88));
	RosettaInterpreterNumberValue hoursError2 = new RosettaInterpreterNumberValue(BigDecimal.valueOf(-2));
	RosettaInterpreterNumberValue minutesError = new RosettaInterpreterNumberValue(BigDecimal.valueOf(88));
	RosettaInterpreterNumberValue secondsError = new RosettaInterpreterNumberValue(BigDecimal.valueOf(88));
	RosettaInterpreterNumberValue minutesError2 = new RosettaInterpreterNumberValue(BigDecimal.valueOf(-2));
	RosettaInterpreterNumberValue secondsError2 = new RosettaInterpreterNumberValue(BigDecimal.valueOf(-2));
	
	RosettaInterpreterTimeValue timeErrorHours = new RosettaInterpreterTimeValue(hoursError, minutes, seconds);
	RosettaInterpreterTimeValue timeErrorHours2 = new RosettaInterpreterTimeValue(hoursError2, minutes, seconds);
	RosettaInterpreterTimeValue timeErrorMinutes = new RosettaInterpreterTimeValue(hours, minutesError, seconds);
	RosettaInterpreterTimeValue timeErrorSeconds = new RosettaInterpreterTimeValue(hours, minutes, secondsError);
	RosettaInterpreterTimeValue timeErrorMinutes2 = new RosettaInterpreterTimeValue(hours, minutesError2, seconds);
	RosettaInterpreterTimeValue timeErrorSeconds2 = new RosettaInterpreterTimeValue(hours, minutes, secondsError2);
	
	@Test
	void getHoursTest() {
		assertEquals(hours, time.getHours());
	}
	
	@Test
	void getMinutesTest() {
		assertEquals(minutes, time.getMinutes());
	}
	
	@Test
	void getSecondsTest() {
		assertEquals(seconds, time.getSeconds());
	}
	
	@Test
	void validTest() {
		assertFalse(timeErrorHours.valid());
		assertFalse(timeErrorHours2.valid());
		assertFalse(timeErrorMinutes.valid());
		assertFalse(timeErrorSeconds.valid());
		assertFalse(timeErrorMinutes2.valid());
		assertFalse(timeErrorSeconds2.valid());
		assertTrue(time.valid());
	}
	
	@Test
	void hashTest() {
		assertEquals(time.hashCode(), time2.hashCode());
		assertNotEquals(time.hashCode(), timeErrorHours.hashCode());
	}
	
	@Test
	void equalsTest() {
		assertTrue(time.equals(time));
		assertTrue(time.equals(time2));
		assertFalse(time.equals(timeErrorHours));
		assertFalse(time.equals(timeErrorMinutes));
		assertFalse(time.equals(timeErrorSeconds));
		assertFalse(time.equals(null));
		assertFalse(time.equals(3));
	}
	
	@Test
	void streamElementTest() {
		assertEquals(List.of(hours, minutes, seconds), time.toElementStream().collect(Collectors.toList()));
	}
	
	@Test
	void streamValueTest() {
		List<RosettaInterpreterTimeValue> stream = Stream.of(time).collect(Collectors.toList());
		assertEquals(stream, time.toValueStream().collect(Collectors.toList()));
	}
	
	@Test
	void toStringTest() {
		assertEquals("RosettaInterpreterTimeValue [hours=RosettaInterpreterNumberValue [5], "
				+ "minutes=RosettaInterpreterNumberValue [30], "
				+ "seconds=RosettaInterpreterNumberValue [28]]", time.toString());
	}
}
