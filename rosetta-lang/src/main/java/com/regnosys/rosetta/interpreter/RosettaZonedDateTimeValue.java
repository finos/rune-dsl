package com.regnosys.rosetta.interpreter;

import java.time.ZonedDateTime;
import java.util.List;

public class RosettaZonedDateTimeValue extends RosettaValueWithNaturalOrder<ZonedDateTime> {
	public RosettaZonedDateTimeValue(List<ZonedDateTime> items) {
		super(items, ZonedDateTime.class);
	}
	
	public static RosettaZonedDateTimeValue of(ZonedDateTime... items) {
		return new RosettaZonedDateTimeValue(List.of(items));
	}
}
