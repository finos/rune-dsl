package com.regnosys.rosetta.interpreternew.values;

import java.math.BigDecimal;

public class RosettaInterpreterNumberValue extends RosettaInterpreterBaseValue {
	private BigDecimal value;
	
	public RosettaInterpreterNumberValue(BigDecimal value) {
		super();
		this.value = value;
	}
	
	public BigDecimal getValue() { return value; }
}
