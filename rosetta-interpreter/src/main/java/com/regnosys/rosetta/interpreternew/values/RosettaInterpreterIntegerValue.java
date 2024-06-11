package com.regnosys.rosetta.interpreternew.values;

import java.math.BigInteger;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterIntegerValue extends RosettaInterpreterBaseValue
	implements Comparable<RosettaInterpreterIntegerValue> {

	private BigInteger value;
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterIntegerValue other = (RosettaInterpreterIntegerValue) obj;
		return Objects.equals(value, other.value);
	}

	public RosettaInterpreterIntegerValue(BigInteger value) {
		super();
		this.value = value;
	}
	
	public RosettaInterpreterIntegerValue(int value) {
		super();
		this.value = BigInteger.valueOf(value);
	}
	
	public BigInteger getValue() { return value; }

	@Override
	public int compareTo(RosettaInterpreterIntegerValue o) {
		 return this.value.compareTo(o.value);
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(value);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
