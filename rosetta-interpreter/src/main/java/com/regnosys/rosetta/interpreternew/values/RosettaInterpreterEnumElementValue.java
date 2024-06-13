package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEnumElementValue extends RosettaInterpreterBaseValue {
	
	private String enumName;
	private String value;
	
	/**
	 * Constructor for an Enum Element Value.
	 *
	 * @param n Name of the Enum
	 * @param v The String value 
	 */
	public RosettaInterpreterEnumElementValue(String n, String v) {
		super();
		this.enumName = n;
		this.value = v;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterEnumElementValue [value=" + value + "]";
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
		RosettaInterpreterEnumElementValue other = (RosettaInterpreterEnumElementValue) obj;
		return Objects.equals(value, other.value) && Objects.equals(enumName, other.enumName);
	}

	public String getValue() { return value; }
	
	public String getEnumName() { return enumName; }

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(value);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
