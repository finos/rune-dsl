package com.regnosys.rosetta.blueprints.runner;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.Issue;
import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class StreamSink<I, O, K> extends NodeImpl implements Downstream<I, K> {
	SinkNode<I, O, K> sink;
	UpstreamList<I,K> upstreamList = new UpstreamList<>();
	
	public StreamSink(SinkNode<I, O, K> sink) {
		super(sink.getURI(), sink.getLabel());
		this.sink = sink;
	}

	@Override
	public <I2 extends I, K2 extends K> void process(GroupableData<I2, K2> input) {
		sink.process(input);
	}

	@Override
	public void terminate() {
		if (upstreamList.terminateUpstream()) {
			sink.terminate();
		}
	}

	public Future<List<Collection<Issue>>> issues() {
		return sink.issues();
	}

	public Future<O> result() {
		return sink.result();
	}

	public Collection<GroupableData<? extends I, K>> getFinalData() {
		return sink.getFinalData();
	}

	@Override
	public void addUpstream(Upstream<? extends I, K> upstream) {
		upstreamList.addUpstream(upstream);
	}

	@Override
	public Collection<Upstream<? extends I, K>> getUpstreams() {
		return upstreamList.getUpstreams();
	}
}
