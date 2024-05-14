package com.regnosys.rosetta.interpreternew.values;

public class RosettaInterpreterErrorValue extends RosettaInterpreterBaseValue{
	private String value;
	
	public RosettaInterpreterErrorValue(String value) {
		super();
		this.value = value;
	}
	
	public String getError() { return value; }
}
