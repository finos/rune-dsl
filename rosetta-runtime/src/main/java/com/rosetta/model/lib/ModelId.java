package com.rosetta.model.lib;

import java.util.Objects;

import com.rosetta.util.DottedPath;

public abstract class ModelId {
	private final DottedPath namespace;	

	public ModelId(DottedPath namespace) {
		Objects.requireNonNull(namespace);
		
		this.namespace = namespace;
	}
	
	public DottedPath getNamespace() {
		return namespace;
	}
	public abstract String getAlphanumericName();
}
