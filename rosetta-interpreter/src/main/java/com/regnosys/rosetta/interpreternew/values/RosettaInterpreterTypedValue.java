package com.regnosys.rosetta.interpreternew.values;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTypedValue extends RosettaInterpreterBaseValue {
	
	public String superType;
	public String name;
	public List<RosettaInterpreterTypedFeatureValue> attributes;

	/**
	 * Constructor for data-type value with no super type.
	 *
	 * @param name 			name value
	 * @param attributes	list of data-type feature values
	 */
	public RosettaInterpreterTypedValue(String name, List<RosettaInterpreterTypedFeatureValue> attributes) {
		super();
		this.superType = null;
		this.name = name;
		this.attributes = attributes;
	}

	/**
	 * Constructor for data-type value with super type.
	 *
	 * @param superType		supertype class
	 * @param name 			name value
	 * @param attributes	list of data-type feature values
	 */
	public RosettaInterpreterTypedValue(String superType, String name,
			List<RosettaInterpreterTypedFeatureValue> attributes) {
		super();
		this.superType = superType;
		this.name = name;
		this.attributes = attributes;
	}
	
	public boolean hasSuperType() {
		return !(superType == null);
	}

	public String getSuperType() {
		return superType;
	}

	public String getName() {
		return name;
	}

	public List<RosettaInterpreterTypedFeatureValue> getAttributes() {
		return attributes;
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(superType, name, attributes);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(superType, attributes, name);
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
		return Objects.equals(attributes, other.attributes) && Objects.equals(name, other.name)
				&& Objects.equals(superType, other.superType);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterTypedValue [supertype=" + superType + ", name=" 
				+ name + ", attributes=" + attributes + "]";
	}
}