package com.regnosys.rosetta.blueprints.runner;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;

import java.util.Optional;


public class StreamSource<O, K> extends Upstream<O, K>{
	SourceNode<O, K> processor;	
	
	public StreamSource(String name, SourceNode<O, K> processor) {
		super(processor.getURI(), processor.getName());
		this.processor = processor;
	}

	public void process() {
		boolean more = true;
		while (more) {
			Optional<GroupableData<O, K>> input = processor.nextItem();
			if (input.isPresent()) {
				downstream.distribute(input.get());
			}
			else more=false;
		}
		downstream.terminate();
	}

	public void setDownstream(DownstreamList<O, K> downstream) {
		this.downstream = downstream;
	}

}
