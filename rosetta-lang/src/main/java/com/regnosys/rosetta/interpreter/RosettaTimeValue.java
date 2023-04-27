package com.regnosys.rosetta.interpreter;

import java.time.LocalTime;
import java.util.List;

public class RosettaTimeValue extends RosettaValueWithNaturalOrder<LocalTime> {
	public RosettaTimeValue(List<LocalTime> items) {
		super(items, LocalTime.class);
	}
	
	public static RosettaTimeValue of(LocalTime... items) {
		return new RosettaTimeValue(List.of(items));
	}
}
