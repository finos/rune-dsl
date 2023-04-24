package com.regnosys.rosetta.types.builtin;

import java.util.Objects;

public class RRecordFeature {
	private final String name;

	public RRecordFeature(String name) {
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
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RRecordFeature other = (RRecordFeature) object;
		return Objects.equals(name, other.name);
	}
}
