package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterBooleanValue extends RosettaInterpreterBaseValue 
	implements Comparable<RosettaInterpreterBooleanValue> {
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterBooleanValue other = (RosettaInterpreterBooleanValue) obj;
		return value == other.value;
	}

	@Override
	public int compareTo(RosettaInterpreterBooleanValue o) {
		return Boolean.compare(this.value, o.value);
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
