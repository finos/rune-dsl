package com.regnosys.rosetta.blueprints.runner.nodes;

public class StatefullNode extends NamedNode {

	protected final boolean publishIntermediate;
	public StatefullNode(String uri, String label, boolean intermediate) {
		super(uri, label);
		publishIntermediate = intermediate;
	}
}
