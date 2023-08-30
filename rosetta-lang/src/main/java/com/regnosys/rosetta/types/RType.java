package com.regnosys.rosetta.types;

import com.rosetta.model.lib.ModelSymbol;

public abstract class RType implements ModelSymbol {
	public boolean hasMeta() {
		return false;
	}
	
	public boolean hasNaturalOrder() {
		return false;
	}
	
	public boolean isBuiltin() {
		return false;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
