package com.regnosys.rosetta.types;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.rosetta.simple.Attribute;

public class RMetaAttribute implements RFeature {
	private final String name;
	private final RType type;

	public RMetaAttribute(String name, RType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public RType getRType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RMetaAttribute other = (RMetaAttribute) obj;
		return Objects.equals(name, other.name) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "RMetaAttribute [name=" + name + ", type=" + type + "]";
	}

	
}
