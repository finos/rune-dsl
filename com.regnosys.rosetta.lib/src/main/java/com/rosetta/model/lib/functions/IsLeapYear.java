package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.time.chrono.IsoChronology;

public class IsLeapYear {
	public boolean execute(Integer year) {
		return IsoChronology.INSTANCE.isLeapYear(year);
	}

	public boolean execute(BigDecimal year) {
		return execute(year.intValue());
	}
}
