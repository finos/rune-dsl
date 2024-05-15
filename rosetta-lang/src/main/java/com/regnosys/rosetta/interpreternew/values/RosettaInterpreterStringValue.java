package com.regnosys.rosetta.interpreternew.values;

public class RosettaInterpreterStringValue extends RosettaInterpreterBaseValue{
	private String value;
	
	public RosettaInterpreterStringValue(String value) {
		super();
		this.value = value;
	}
	
	public String getValue() { return value; }
}
