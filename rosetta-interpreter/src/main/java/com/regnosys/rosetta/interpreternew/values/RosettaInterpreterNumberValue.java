package com.regnosys.rosetta.interpreternew.values;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

import com.rosetta.model.lib.RosettaNumber;

public class RosettaInterpreterNumberValue extends RosettaInterpreterBaseValue 
	implements Comparable<RosettaInterpreterNumberValue> { 
	private RosettaNumber value;

	
	public RosettaInterpreterNumberValue(BigDecimal value) {
		super();
		this.value = RosettaNumber.valueOf(value);
	}
	
	public RosettaInterpreterNumberValue(RosettaNumber value) {
		super();
		this.value = value;
	}
	
	public RosettaInterpreterNumberValue(double value) {
		super();
		this.value = RosettaNumber.valueOf(value);
	}

	public RosettaNumber getValue() { return value; }

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
		RosettaInterpreterNumberValue other = (RosettaInterpreterNumberValue) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public int compareTo(RosettaInterpreterNumberValue o) {
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
	
	@Override 
	public String toString() {
		return "RosettaInterpreterNumberValue [" + value.toString() + "]";
	}
	
}
