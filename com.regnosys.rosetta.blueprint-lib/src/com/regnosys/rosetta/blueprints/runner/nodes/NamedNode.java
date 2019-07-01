package com.regnosys.rosetta.blueprints.runner.nodes;

public abstract class NamedNode {
	private final String uri;
	private final String label;

	public NamedNode(String uri, String label) {
		this.uri = uri;
		this.label = label;
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
}
