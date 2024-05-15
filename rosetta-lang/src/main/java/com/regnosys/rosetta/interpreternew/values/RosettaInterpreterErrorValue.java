package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

public class RosettaInterpreterErrorValue extends RosettaInterpreterBaseValue{
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
		RosettaInterpreterErrorValue other = (RosettaInterpreterErrorValue) obj;
		return Objects.equals(value, other.value);
	}

	private String value;
	
	public RosettaInterpreterErrorValue(String value) {
		super();
		this.value = value;
	}
	
	public String getError() { return value; }
}
