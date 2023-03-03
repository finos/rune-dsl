package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;

public abstract class NamedNode {
	private final String uri;
	private final String label;
	private final DataIdentifier identifier;

	public NamedNode(String uri, String label, DataIdentifier identifier) {
		this.uri = uri;
		this.label = label;
		this.identifier = identifier;
	}
	
	public String getName() {
		return label;
	}

	public String getURI() {
		return uri;
	}
	
	public String getLabel() {
		return label;
	}

	public DataIdentifier getIdentifier() {
		return identifier;
	}
}
