package com.regnosys.rosetta.interpreternew.values;

import java.math.BigDecimal;
import java.util.Objects;

public class RosettaInterpreterNumberValue extends RosettaInterpreterBaseValue 
	implements Comparable<RosettaInterpreterNumberValue>{
	private BigDecimal value;
	
	public RosettaInterpreterNumberValue(BigDecimal value) {
		super();
		this.value = value;
	}
	
	public BigDecimal getValue() { return value; }

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
		RosettaInterpreterNumberValue other = (RosettaInterpreterNumberValue) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public int compareTo(RosettaInterpreterNumberValue o) {
		return this.value.compareTo(o.value);
	}
	
}
