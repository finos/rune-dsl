package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.Objects;

import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.types.RType;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public abstract class RRecordType extends RType {
	private final ModelSymbolId symbolId;
	
	public RRecordType(String name) {
		super();
		this.symbolId = new ModelSymbolId(DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE), name);
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return this.symbolId;
	}
	@Override
	public boolean hasNaturalOrder() {
		return true;
	}
	
	// TODO: is this necessary?
	public abstract Collection<RRecordFeature> getFeatures();
	
	@Override
	public int hashCode() {
		return Objects.hash(getSymbolId(), getFeatures());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RRecordType other = (RRecordType) object;
		return Objects.equals(getSymbolId(), other.getSymbolId())
				&& Objects.equals(getFeatures(), other.getFeatures());
	}
}
