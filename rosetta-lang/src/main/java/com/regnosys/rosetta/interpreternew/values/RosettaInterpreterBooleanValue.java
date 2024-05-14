package com.regnosys.rosetta.interpreternew.values;


public class RosettaInterpreterBooleanValue extends RosettaInterpreterBaseValue{
	private boolean value;
	
	public RosettaInterpreterBooleanValue(boolean value) {
		super();
		this.value = value;
	}
	
	public boolean getValue() { return value; }
}
