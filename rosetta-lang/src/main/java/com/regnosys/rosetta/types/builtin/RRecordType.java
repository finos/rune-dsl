package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.Objects;

public abstract class RRecordType extends RBuiltinType {	
	public RRecordType(String name) {
		super(name);
	}
	
	public abstract Collection<RRecordFeature> getFeatures();
	
	@Override
	public int hashCode() {
		return Objects.hash(getName(), getFeatures());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RRecordType other = (RRecordType) obj;
		return Objects.equals(getName(), other.getName())
				&& Objects.equals(getFeatures(), other.getFeatures());
	}
}
