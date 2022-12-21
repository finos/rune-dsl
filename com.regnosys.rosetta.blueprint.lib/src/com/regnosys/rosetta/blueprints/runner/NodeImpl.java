package com.regnosys.rosetta.blueprints.runner;

public abstract class NodeImpl implements StreamNode{
	
	private final String URI;
	private final String label;

	public NodeImpl(String uri, String label) {
		super();
		URI = uri;
		this.label = label;
	}

	@Override
	public String getURI() {
		return URI;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
