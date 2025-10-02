package com.regnosys.rosetta.generator.java.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;

public class RJavaEnumValue {
	private final RJavaEnum enumeration;
	
	private final String name;
	private final RosettaEnumValue value;
	
	private final RJavaEnumValue parentValue;

	public RJavaEnumValue(RJavaEnum enumeration, String name, RosettaEnumValue value, RJavaEnumValue parentValue) {
		this.enumeration = enumeration;
		this.name = name;
		this.value = value;
		this.parentValue = parentValue;
	}

	public RJavaEnum getEnumeration() {
		return enumeration;
	}
	
	public RosettaEnumValue getEObject() {
		return value;
	}
	
	public String getName() {
		return name;
	}

	public String getRosettaName() {
		return value.getName();
	}

	public String getDisplayName() {
		return value.getDisplay();
	}

	public RJavaEnumValue getParentValue() {
		return parentValue;
	}

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
		RJavaEnumValue other = (RJavaEnumValue) obj;
		return Objects.equals(value, other.value);
	}
}
