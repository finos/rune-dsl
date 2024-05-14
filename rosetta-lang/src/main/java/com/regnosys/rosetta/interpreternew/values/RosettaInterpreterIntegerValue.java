package com.regnosys.rosetta.interpreternew.values;

import java.math.BigInteger;

public class RosettaInterpreterIntegerValue extends RosettaInterpreterBaseValue{
	private BigInteger value;
	
	public RosettaInterpreterIntegerValue(BigInteger value) {
		super();
		this.value = value;
	}
	
	public BigInteger getValue() { return value; }
}
