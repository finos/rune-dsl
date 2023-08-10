package com.regnosys.rosetta.types;

import com.rosetta.util.DottedPath;

public abstract class RType {
	public abstract String getName();
	
	public abstract DottedPath getNamespace();

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
