package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;

public class RosettaDateItem extends RosettaValueItemWithNaturalOrder<LocalDate> {
	public RosettaDateItem(LocalDate value) {
		super(value);
	}
}
