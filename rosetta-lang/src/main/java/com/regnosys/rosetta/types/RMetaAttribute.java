package com.regnosys.rosetta.types;

public class RMetaAttribute {
	private final String name;
	private final RType type;

	public RMetaAttribute(String name, RType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	
}
