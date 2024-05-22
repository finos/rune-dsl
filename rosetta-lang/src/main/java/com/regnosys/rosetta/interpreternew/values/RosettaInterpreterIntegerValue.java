package com.regnosys.rosetta.interpreternew.values;

import java.math.BigInteger;
import java.util.Objects;

public class RosettaInterpreterIntegerValue extends RosettaInterpreterBaseValue
	implements Comparable<RosettaInterpreterIntegerValue> {
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
		RosettaInterpreterIntegerValue other = (RosettaInterpreterIntegerValue) obj;
		return Objects.equals(value, other.value);
	}

	private BigInteger value;
	
	public RosettaInterpreterIntegerValue(BigInteger value) {
		super();
		this.value = value;
	}
	
	public BigInteger getValue() { return value; }

	@Override
	public int compareTo(RosettaInterpreterIntegerValue o) {
		 return this.value.compareTo(o.value);
	}
	
	@Override
	public RosettaInterpreterBaseValue createInstance() { 
		return new RosettaInterpreterIntegerValue(this.value);
	}
}
