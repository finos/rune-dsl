package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;
import java.util.List;

public class RosettaDateValue extends RosettaValueWithNaturalOrder<LocalDate> {
	public RosettaDateValue(List<LocalDate> items) {
		super(items, LocalDate.class);
	}
	
	public static RosettaDateValue of(LocalDate... items) {
		return new RosettaDateValue(List.of(items));
	}
}
