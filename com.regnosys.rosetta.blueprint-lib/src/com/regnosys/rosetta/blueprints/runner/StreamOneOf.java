package com.regnosys.rosetta.blueprints.runner;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.blueprints.BlueprintInstance;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public class StreamOneOf <I, O, K1 extends Comparable<K1>, K2 extends Comparable<K2>> extends Upstream<O, K2> implements Downstream<I, K1>{
	
	UpstreamList<I, K1> upstreamList = new UpstreamList<>();
	private List<BlueprintInstance<? super I, ? extends Object, K1, K1>> filters;
	private List<BlueprintInstance<? super I, ? extends O, K1, K2>> thens;
	CaptureDownstream<Object, K1> filterDownstream = new CaptureDownstream<>();

	public StreamOneOf(String uri, String label, List<BlueprintInstance<? super I, ? extends Object, K1, K1>> filters, List<BlueprintInstance<? super I, ? extends O, K1, K2>> thens) {
		super(uri, label);
		if (thens.size()< filters.size() || thens.size()>filters.size()+1) {
			throw new RuntimeException ("must have one then clause for each if clause and a single else clause");
		}
		this.filters = filters;
		filters.forEach(f->f.addDownstreams(filterDownstream));
		this.thens = thens;
	}

	@Override
	public <I2 extends I> void process(GroupableData<I2, K1> input) {
		for (int i=0;i<filters.size();i++) {
			BlueprintInstance<? super I, ? extends Object, K1, K1> filter = filters.get(i);
			filter.process(input);
			boolean result = filterDownstream.isTruthy(input.getKey());
			if (result) {
				thens.get(i).process(input);
				return;
			}
		}
		if (thens.size()>filters.size()) {
			thens.get(thens.size()-1).process(input);
		}
	}

	@Override
	public void terminate() {
		thens.forEach(t->t.terminate());
	}

	@Override
	public void addUpstream(Upstream<? extends I, K1> upstream) {
		upstreamList.addUpstream(upstream);
	}

	@Override
	public Collection<Upstream<? extends I, K1>> getUpstreams() {
		return upstreamList.getUpstreams();
	}

}
