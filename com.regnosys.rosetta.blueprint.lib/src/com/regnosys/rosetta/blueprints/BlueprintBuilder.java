package com.regnosys.rosetta.blueprints;

import static com.google.common.collect.ImmutableList.of;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.blueprints.runner.Downstream;
import com.regnosys.rosetta.blueprints.runner.StreamExpander;
import com.regnosys.rosetta.blueprints.runner.StreamProcessor;
import com.regnosys.rosetta.blueprints.runner.StreamSink;
import com.regnosys.rosetta.blueprints.runner.StreamSource;
import com.regnosys.rosetta.blueprints.runner.Upstream;
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
import com.regnosys.rosetta.blueprints.runner.nodes.ExpanderNode;
import com.regnosys.rosetta.blueprints.runner.nodes.Node;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;

public class BlueprintBuilder<I,O, K1, K2> {
	

	private final RosettaActionFactory actionFactory;
	private final Collection<Downstream<? super I,K1>> heads;
	private final Collection<Upstream<? extends O,K2>> tails;
	
	private final Collection<StreamSink<?,?,?>> sinks;
	private final Collection<StreamSource<?,?>> sources;
	private final DataItemReportBuilder dataItemReportBuilder;
	
	
	private BlueprintBuilder(RosettaActionFactory actionFactory, 
			Collection<Downstream<? super I, K1>> heads, 
			Collection<Upstream<? extends O, K2>> tails,
			Collection<StreamSink<?, ?, ?>> sinks, 
			Collection<StreamSource<?,?>> sources,
			DataItemReportBuilder dataItemReportBuilder) {
		this.actionFactory = actionFactory;
		this.heads = heads;
		this.tails = tails;
		this.sinks = sinks;
		this.sources = sources;
		this.dataItemReportBuilder = dataItemReportBuilder;
	}
	
	public RosettaActionFactory getRosettaActionFactory() {
		return Objects.requireNonNull(actionFactory, "BlueprintBuilder.init must be called with a valid RosettaActionFactory");
	}
	
	@SuppressWarnings("unchecked")
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> startsWith(RosettaActionFactory actionFactory, Node<I, O, K1, K2> n) {
		if (n instanceof ProcessorNode) {
			return (BlueprintBuilder<I, O, K1, K2>) startsWith(actionFactory, (ProcessorNode<I, O, K1>)n);
		}
		if (n instanceof ExpanderNode) {
			return (BlueprintBuilder<I, O, K1, K2>) startsWith(actionFactory, (ExpanderNode<I, O, K1>)n);
		}
		if (n instanceof BlueprintInstance) {
			return startsWith(actionFactory, (BlueprintInstance<I, O, K1, K2>)n);
		}
		throw new UnsupportedOperationException("Don't know how to start a blueprint with "+n.getClass().getSimpleName());
	}
	
	public static <O, K> BlueprintBuilder<Void,O,K, K> startsWith(RosettaActionFactory actionFactory, SourceNode<O, K> sourceNode) {
		StreamSource<O,K> source = new StreamSource<>(sourceNode.getName(),sourceNode);
		return new BlueprintBuilder<>(actionFactory, Collections.emptyList(), of(source), of(), of(source), null);
	}
	
	public static <I, O, K> BlueprintBuilder<I,O,K, K> startsWith(RosettaActionFactory actionFactory, ExpanderNode<I,O,K> expandNode) {
		StreamExpander<I, O, K> expand = new StreamExpander<>(expandNode);
		return new BlueprintBuilder<>(actionFactory, of(expand), of(expand), of(), of(), null);
	}

	public static <I, O, K> BlueprintBuilder<I,O, K, K> startsWith(RosettaActionFactory actionFactory, ProcessorNode<I, O, K> processorNode) {
		StreamProcessor<I, O, K> group = new StreamProcessor<>(processorNode);
		return new BlueprintBuilder<>(actionFactory, of(group), of(group), of(), of(), null);
	}
	
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> startsWith(RosettaActionFactory actionFactory, BlueprintInstance<I, O, K1, K2> bp2) {
		return new BlueprintBuilder<>(actionFactory, Collections.singletonList(bp2), Collections.singletonList(bp2), bp2.sinks, bp2.sources, null);
	}
	
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> startsWith(RosettaActionFactory actionFactory, BlueprintBuilder<I, O, K1, K2> bpb) {
		return bpb;
	}

	public <O2> BlueprintBuilder<I, O2, K1, K2> then(ExpanderNode<? super O,O2,K2> expandNode) {
		StreamExpander<O, O2, K2> expand = new StreamExpander<>(expandNode);
		//add new node downstream of existing tails
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(expand);
		}
		return new BlueprintBuilder<I, O2, K1, K2>(actionFactory, heads, of(expand), of(), sources, dataItemReportBuilder);
	}
	public <O2> BlueprintBuilder<I, O2, K1, K2> then(ProcessorNode<? super O, O2, K2> procesorNode) {
		StreamProcessor<? super O, O2, K2> process = new StreamProcessor<>(procesorNode);
		//add new node downstream of existing tails
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(process);
		}
		return new BlueprintBuilder<I, O2, K1, K2>(actionFactory, heads, of(process), of(), sources, dataItemReportBuilder);
	}
	
	public <O2, K3> BlueprintBuilder<I, O2, K1, K3> then(BlueprintBuilder<O, O2, K2, K3> bp2) {
		
		//add all the heads of bp2 as downstream of this BP
		for (Upstream<? extends O, K2> up: tails) {
			for (Downstream<? super O, K2> down: bp2.heads) {
				up.addDownstreams(down);
			}
		}
		//merge the sources and sinks lists
		Collection<StreamSink<?, ?, ?>> sinks = 
				ImmutableList.<StreamSink<?, ?, ?>>builder().addAll(this.sinks).addAll(bp2.sinks).build();;
		Collection<StreamSource<?,?>> sources = 
				ImmutableList.<StreamSource<?,?>>builder().addAll(this.sources).addAll(bp2.sources).build();
		return new BlueprintBuilder<>(actionFactory, heads, bp2.tails, sinks, sources, dataItemReportBuilder);
	}
	
	public <O2, K3> BlueprintBuilder<I, O2, K1, K3> then(BlueprintInstance<? super O, O2, K2, K3> bp2) {
		
		//adds the blueprint as a sub graph
		tails.forEach(t->t.addDownstreams(bp2));
		Collection<StreamSink<?, ?, ?>> sinks = 
				ImmutableList.<StreamSink<?, ?, ?>>builder().addAll(this.sinks).addAll(bp2.getSinks()).build();;
		Collection<StreamSource<?,?>> sources = 
				ImmutableList.<StreamSource<?,?>>builder().addAll(this.sources).addAll(bp2.getSources()).build();
		return new BlueprintBuilder<>(actionFactory, heads, Collections.singletonList(bp2), sinks, sources, bp2.getDataItemReportBuilder());
	}
	
	public BlueprintBuilder<I,O, K1, K2> andSink(SinkNode<? super O, ?, K2> sinkNode) {
		StreamSink<? super O, ?, K2> sink = new StreamSink<>(sinkNode);
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(sink);
		}
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, heads, of(), of(sink), sources, dataItemReportBuilder);
	}
	
	
	@SafeVarargs
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> or(RosettaActionFactory actionFactory, BlueprintBuilder<I, ? extends O, K1, K2>... bps) {
		Collection<Downstream<? super I, K1>> heads = Arrays.stream(bps).flatMap(bp->bp.heads.stream()).collect(ImmutableList.toImmutableList());
		Collection<Upstream<? extends O, K2>> tails = Arrays.stream(bps).flatMap(bp->bp.tails.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSink<?, ?, ?>> sinks = Arrays.stream(bps).flatMap(bp->bp.sinks.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSource<?,?>> sources = Arrays.stream(bps).flatMap(bp->bp.sources.stream()).collect(ImmutableList.toImmutableList());
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, heads, tails, sinks, sources, null);
	}
	
	public BlueprintBuilder<I,O, K1, K2> addDataItemReportBuilder(DataItemReportBuilder dataItemReportBuilder) {
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, heads, tails, sinks, sources, dataItemReportBuilder);
	}
	
	public BlueprintInstance<I, O, K1, K2> toBlueprint(String uri, String blueprintName) {
		return new BlueprintInstance<I, O, K1, K2>(uri, blueprintName, heads, tails, sources, sinks, dataItemReportBuilder);
	}	
}
