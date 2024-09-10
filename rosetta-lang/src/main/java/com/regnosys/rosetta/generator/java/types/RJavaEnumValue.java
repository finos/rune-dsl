package com.regnosys.rosetta.generator.java.types;

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
}
