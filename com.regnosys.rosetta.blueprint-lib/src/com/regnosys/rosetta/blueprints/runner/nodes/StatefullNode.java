package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;

public class StatefullNode extends NamedNode {

	protected final boolean publishIntermediate;
	public StatefullNode(String uri, String label, boolean intermediate, DataIdentifier identifier) {
		super(uri, label, identifier);
		publishIntermediate = intermediate;
	}
}
