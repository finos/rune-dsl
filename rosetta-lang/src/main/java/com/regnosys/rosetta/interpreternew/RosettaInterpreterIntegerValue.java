package com.regnosys.rosetta.interpreternew;

public class RosettaInterpreterIntegerValue extends RosettaInterpreterValue{
	private int value;
	
	public RosettaInterpreterIntegerValue(int value) {
		super();
		this.value = value;
	}
	
	public int getValue() { return value; }
}
