package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

public class RosettaInterpreterStringValue extends RosettaInterpreterBaseValue{
	private String value;
	
	public RosettaInterpreterStringValue(String value) {
		super();
		this.value = value;
	}
	
	public String getValue() { return value; }

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RosettaInterpreterStringValue other = (RosettaInterpreterStringValue) obj;
		return Objects.equals(value, other.value);
	}
	
}
