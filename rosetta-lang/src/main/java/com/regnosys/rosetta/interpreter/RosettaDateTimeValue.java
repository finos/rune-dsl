package com.regnosys.rosetta.interpreter;

import java.time.LocalDateTime;
import java.util.List;

public class RosettaDateTimeValue extends RosettaValueWithNaturalOrder<LocalDateTime> {
	public RosettaDateTimeValue(List<LocalDateTime> items) {
		super(items, LocalDateTime.class);
	}
	
	public static RosettaDateTimeValue of(LocalDateTime... items) {
		return new RosettaDateTimeValue(List.of(items));
	}
}
