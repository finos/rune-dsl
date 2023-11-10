package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

public class RAttribute implements RAssignedRoot {
	private String name;
	private String definition;
	private RType rType;
	private List<RAttribute> metaAnnotations;
	private boolean isMulti;

	public RAttribute(String name, String definition, RType rType, List<RAttribute> metaAnnotations, boolean isMulti) {
		this.name = name;
		this.definition = definition;
		this.rType = rType;
		this.metaAnnotations = metaAnnotations;
		this.isMulti = isMulti;
	}
	
	@Override
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
	
	
	public List<RAttribute> getMetaAnnotations() {
		return metaAnnotations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(definition, isMulti, metaAnnotations, name, rType);
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
				&& Objects.equals(metaAnnotations, other.metaAnnotations) && Objects.equals(name, other.name)
				&& Objects.equals(rType, other.rType);
	}



}
