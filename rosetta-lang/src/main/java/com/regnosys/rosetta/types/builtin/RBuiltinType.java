package com.regnosys.rosetta.types.builtin;

import java.util.Objects;

import com.regnosys.rosetta.types.RAnnotateType;

public abstract class RBuiltinType extends RAnnotateType {	
	private final String name;
	protected RBuiltinType(final String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RBuiltinType other = (RBuiltinType) obj;
		return Objects.equals(name, other.name);
	}
}
