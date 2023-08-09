package com.regnosys.rosetta.types;

import java.util.Objects;

public class RAttribute {
	private String name;
	private String definition;
	private RType rType;
	private boolean isMulti;

	public RAttribute(String name, String definition, RType rType, boolean isMulti) {
		this.name = name;
		this.definition = definition;
		this.rType = rType;
		this.isMulti = isMulti;
	}

	public String getName() {
		return name;
	}

	public RType getRType() {
		return rType;
	}

	public boolean isMulti() {
		return isMulti;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(definition, isMulti, name, rType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RAttribute other = (RAttribute) obj;
		return Objects.equals(definition, other.definition) && isMulti == other.isMulti
				&& Objects.equals(name, other.name) && Objects.equals(rType, other.rType);
	}

}
