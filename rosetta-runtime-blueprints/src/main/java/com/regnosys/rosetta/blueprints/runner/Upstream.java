package com.regnosys.rosetta.blueprints.runner;

import java.util.List;

public abstract class Upstream<O, K> extends NodeImpl {
	protected DownstreamList<O, K> downstream;
	
	//Marks a StreamNode that Produces O that can be upstream of a node that consumes O
	
	public Upstream(String uri, String label) {
		super(uri, label);
		downstream = new DownstreamList<O, K>();
		
	}

	@SafeVarargs
	public final void addDownstreams(Downstream<? super O, K>... downstreams) {
		for (Downstream<? super O, K> d:downstreams) {
			downstream.addDownstream(d);
			d.addUpstream(this);
		}
	}

	public List<StreamNode> getDownstream() {
		return downstream.getNodes();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+ " " + getLabel();
	}

}
