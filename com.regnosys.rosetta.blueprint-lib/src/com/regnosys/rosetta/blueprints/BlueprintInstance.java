package com.regnosys.rosetta.blueprints;

import com.regnosys.rosetta.blueprints.runner.Downstream;
import com.regnosys.rosetta.blueprints.runner.StreamSink;
import com.regnosys.rosetta.blueprints.runner.StreamSource;
import com.regnosys.rosetta.blueprints.runner.Upstream;
import com.regnosys.rosetta.blueprints.runner.UpstreamList;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BlueprintInstance <I, O, K1 , K2> extends Upstream<O, K2> implements Downstream<I, K1>{

	protected final Collection<StreamSink<?,?,?>> sinks;
	protected final Collection<StreamSource<?,?>> sources;
	
	private final ReportTypeBuilder reportTypeBuilder;
	
	private final InternalHead internalHead;
	private final InternalTail internalTail;
	
	private final UpstreamList<I, K1> upstreamList = new UpstreamList<>();



	public BlueprintInstance(String uri, String label, Collection<Downstream<? super I, K1>> heads,
    		Collection<Upstream<? extends O, K2>> tails, Collection<StreamSource<?, ?>> sources,
			Collection<StreamSink<?, ?, ?>> sinks, ReportTypeBuilder reportTypeBuilder) {
		super(uri, label);
		this.sinks = sinks;
		this.sources = sources;
		this.reportTypeBuilder = reportTypeBuilder;
		internalTail = new InternalTail();
		tails.forEach(t->t.addDownstreams(internalTail));

		internalHead =new InternalHead();
		heads.forEach(h->internalHead.addDownstreams(h));
	}
    


	@Override
	public <I2 extends I, KI extends K1> void process(GroupableData<I2, KI> input) {
		internalHead.process(input);
	}

	@Override
	public void terminate() {
		if (upstreamList.terminateUpstream()) {
			internalHead.terminate();
		}
	}

	@Override
	public void addUpstream(Upstream<? extends I, K1> upstream) {
		upstreamList.addUpstream(upstream);
	}

    public Collection<StreamSource<?, ?>> getSources() {
        return sources;
    }

    public Collection<StreamSink<?, ?, ?>> getSinks() {
        return sinks;
    }
    
    public class InternalHead extends Upstream<I, K1> implements Downstream<I, K1> {

    	//DownstreamList<I, K1> downstream = new DownstreamList<>();//this contains all the first nodes of the BP
    	
    	
		public InternalHead() {
			super(BlueprintInstance.this.getURI(),	BlueprintInstance.this.getLabel()+"internalHead");
		}

		@Override
		public <I2 extends I, KI extends K1> void process(GroupableData<I2, KI> input) {
			downstream.distribute(input);
		}

		@Override
		public void terminate() {
			//only ever has one upsrtream - the BP itself
			downstream.terminate();
		}

		@Override
		public void addUpstream(Upstream<? extends I, K1> upstream) {
		}

		@Override
		public Collection<Upstream<? extends I, K1>> getUpstreams() {
			return BlueprintInstance.this.getUpstreams();
		}
    }
	
	public class InternalTail extends Upstream<O, K2> implements Downstream<O, K2>{

		UpstreamList<O, K2> upstreamList = new UpstreamList<>();//this contains all the last nodes in the BP
		
		public InternalTail() {
			super(BlueprintInstance.this.getURI(),	BlueprintInstance.this.getLabel()+"internalTail");
		}

		@Override
		public void terminate() {
			if (upstreamList.terminateUpstream()) {
				BlueprintInstance.this.downstream.terminate();
			}
		}

		@Override
		public Collection<Upstream<? extends O, K2>> getUpstreams() {
			return upstreamList.getUpstreams();
		}
		
		public BlueprintInstance<I, O, K1, K2> getBlueprint() {
			return BlueprintInstance.this;
		}

		@Override
		public <I2 extends O, KO extends K2> void process(GroupableData<I2, KO> input) {
			BlueprintInstance.this.downstream.distribute(input);
		}

		@Override
		public void addUpstream(Upstream<? extends O, K2> upstream) {
			upstreamList.addUpstream(upstream);
		}
	}
	
	public BlueprintReport runBlueprint() throws InterruptedException, ExecutionException {

        sources.stream().distinct().forEach(s->s.process());

        Object reportData = sinks.stream().map(s->s.result()).findFirst().get().get();
        Collection<GroupableData<? extends Object, ?>> traceData = sinks.stream().flatMap(s->s.getFinalData().stream()).collect(Collectors.toList());
        return new BlueprintReport(getLabel(), reportData, traceData, reportBuilder);
    }

	public Upstream<I, K1> getInternalHead() {
		return internalHead;
	}

	public Downstream<O, K2> getInternalTail() {
		return internalTail;
	}

	@Override
	public Collection<Upstream<? extends I, K1>> getUpstreams() {
		return upstreamList.getUpstreams();
	}
	
	public ReportTypeBuilder getReportTypeBuilder() {
		return reportTypeBuilder;
	}
}
