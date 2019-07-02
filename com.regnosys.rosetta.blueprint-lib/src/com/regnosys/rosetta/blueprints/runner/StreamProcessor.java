package com.regnosys.rosetta.blueprints.runner;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

import java.util.Collection;
import java.util.Optional;
//import org.apache.log4j.Logger;

public class StreamProcessor<I,O, K extends Comparable<K>> extends Upstream<O, K> implements Downstream<I, K>{
	//private final static Logger logger = Logger.getLogger(StreamProcessor.class);
	
	ProcessorNode<? super I, O, K> processor;
	UpstreamList<I, K> upstreamList = new UpstreamList<>();
	
	public StreamProcessor(ProcessorNode<? super I, O, K> processor) {
		super(processor.getURI(), processor.getName());
		this.processor = processor;
	}

	@Override
	public <I2 extends I> void process(GroupableData<I2, K> input) {
		Optional<GroupableData<O, K>> output = processor.process(input);
		if (output.isPresent()) {
			downstream.distribute(output.get());
		}
	}

	@Override
	public void terminate() {
		if (upstreamList.terminateUpstream()) {
			Collection<GroupableData<? extends O, K>> terminal = processor.terminate();
			for (GroupableData<? extends O, K> data:terminal) {
				downstream.distribute(data);
			}
			downstream.terminate();
		}
		else {
			//logger.debug(("Not all parents done for "+getName()));
		}
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
