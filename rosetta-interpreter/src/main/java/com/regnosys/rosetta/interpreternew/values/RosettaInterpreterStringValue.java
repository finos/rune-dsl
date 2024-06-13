package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterStringValue extends RosettaInterpreterBaseValue 
	implements Comparable<RosettaInterpreterStringValue> {
	
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterStringValue other = (RosettaInterpreterStringValue) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public int compareTo(RosettaInterpreterStringValue o) {
		int compareValue = this.value.compareTo(o.value);
		if (compareValue < 0) {
			return -1;
		}
		else if (compareValue > 0) {
			return 1;
		} 
		else {
			return 0;
		}
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
		return "RosettaInterpreterStringValue [" + value + "]";
	}
	
}
