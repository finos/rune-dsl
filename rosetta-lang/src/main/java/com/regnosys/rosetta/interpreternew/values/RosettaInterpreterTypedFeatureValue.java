package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTypedFeatureValue extends RosettaInterpreterBaseValue {
	
	public String name;
	public RosettaInterpreterValue value;
	//public RosettaCardinality card;

	public RosettaInterpreterTypedFeatureValue(String name, RosettaInterpreterValue value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public RosettaInterpreterValue getValue() {
		return value;
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(name, value);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
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
		RosettaInterpreterTypedFeatureValue other = (RosettaInterpreterTypedFeatureValue) obj;
		return Objects.equals(name, other.name) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterTypedFeatureValue [name=" + name + ", value=" + value + "]";
	}

}
