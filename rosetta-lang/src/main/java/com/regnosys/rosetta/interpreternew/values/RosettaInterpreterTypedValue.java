package com.regnosys.rosetta.interpreternew.values;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTypedValue extends RosettaInterpreterBaseValue {
	
	public String name;
	public List<RosettaInterpreterTypedFeatureValue> attributes;

	public RosettaInterpreterTypedValue(String name, List<RosettaInterpreterTypedFeatureValue> attributes) {
		super();
		this.name = name;
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public List<RosettaInterpreterTypedFeatureValue> getAttributes() {
		return attributes;
	}
	
	public List<String> getAttributesNames() {
		List<String> names = new ArrayList<>();
		
		for (RosettaInterpreterTypedFeatureValue att : attributes) {
			names.add(att.getName());
		}
		
		return names;
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(name, attributes);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, name);
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
		RosettaInterpreterTypedValue other = (RosettaInterpreterTypedValue) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterTypedValue [name=" + name + ", attributes=" + attributes + "]";
	}

}
