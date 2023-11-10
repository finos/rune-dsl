package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.chrono.IsoChronology;

public class IsLeapYear {
	public boolean execute(Integer year) {
		return execute(year.longValue());
	}
	
	public boolean execute(Long year) {
		return IsoChronology.INSTANCE.isLeapYear(year);
	}
	
	public boolean execute(BigInteger year) {
		return execute(year.longValue());
	}

	public boolean execute(BigDecimal year) {
		return execute(year.longValue());
	}
}
