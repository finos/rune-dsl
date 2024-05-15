package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

public class RosettaInterpreterBooleanValue extends RosettaInterpreterBaseValue{
	private boolean value;
	
	public RosettaInterpreterBooleanValue(boolean value) {
		super();
		this.value = value;
	}
	
	public boolean getValue() { return value; }

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
		RosettaInterpreterBooleanValue other = (RosettaInterpreterBooleanValue) obj;
		return value == other.value;
	}
	
	
}
