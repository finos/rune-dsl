package com.regnosys.rosetta.interpreternew.values;

public class RosettaInterpreterIntegerValue extends RosettaInterpreterBaseValue{
	private int value;
	
	public RosettaInterpreterIntegerValue(int value) {
		super();
		this.value = value;
	}
	
	public int getValue() { return value; }
}
