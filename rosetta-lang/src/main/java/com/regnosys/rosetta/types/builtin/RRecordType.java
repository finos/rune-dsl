package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.Objects;

import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.types.RType;
import com.rosetta.util.DottedPath;

public abstract class RRecordType extends RType {
	private final String name;
	
	public RRecordType(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public DottedPath getNamespace() {
		return DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE);
	}
	@Override
	public boolean hasNaturalOrder() {
		return true;
	}
	
	// TODO: is this necessary?
	public abstract Collection<RRecordFeature> getFeatures();
	
	@Override
	public int hashCode() {
		return Objects.hash(getName(), getFeatures());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RRecordType other = (RRecordType) object;
		return Objects.equals(name, other.name)
				&& Objects.equals(getFeatures(), other.getFeatures());
	}
}
