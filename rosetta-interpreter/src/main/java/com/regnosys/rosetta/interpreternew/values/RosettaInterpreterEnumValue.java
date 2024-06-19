package com.regnosys.rosetta.interpreternew.values;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEnumValue extends RosettaInterpreterBaseValue {
	
	private String name;
	private List<RosettaInterpreterValue> values;
	
	/**
	 * Constructor for an Enum Value.
	 *
	 * @param name Name of the Enum
	 * @param values A list of all the String values that the Enum accepts 
	 */
	public RosettaInterpreterEnumValue(String name, List<RosettaInterpreterValue> values) {
		super();
		this.name = name;
		this.values = values;
	}
	
	/**
	 * Method that checks that the enum contains a certain value.
	 *
	 * @param name Name of the value that the enum should contain
	 */
	public boolean containsValueName(String name) {
		for (RosettaInterpreterValue v : values) {
			if (((RosettaInterpreterEnumElementValue) v).getValue().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public List<RosettaInterpreterValue> getValues() { return values; }
	
	public String getName() { return name; }
	
	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterListValue [name = " + name + ", values = " + values.toString() + "]";
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
		RosettaInterpreterEnumValue other = (RosettaInterpreterEnumValue) obj;
		return Objects.equals(values, other.values) && Objects.equals(name, other.name);
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(values.toArray());
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
