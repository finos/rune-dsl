package com.regnosys.rosetta.blueprints;

import static com.google.common.collect.ImmutableList.of;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.blueprints.runner.Downstream;
import com.regnosys.rosetta.blueprints.runner.StreamDataJoin;
import com.regnosys.rosetta.blueprints.runner.StreamExpander;
import com.regnosys.rosetta.blueprints.runner.StreamGroup;
import com.regnosys.rosetta.blueprints.runner.StreamOneOf;
import com.regnosys.rosetta.blueprints.runner.StreamProcessor;
import com.regnosys.rosetta.blueprints.runner.StreamSink;
import com.regnosys.rosetta.blueprints.runner.StreamSource;
import com.regnosys.rosetta.blueprints.runner.Upstream;
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
import com.regnosys.rosetta.blueprints.runner.nodes.DataJoinNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ExpanderNode;
import com.regnosys.rosetta.blueprints.runner.nodes.GroupNode;
import com.regnosys.rosetta.blueprints.runner.nodes.Node;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.MappingGroup;

public class BlueprintBuilder<I,O, K1, K2> {
	

	private final RosettaActionFactory actionFactory;
	private final Collection<Downstream<? super I,K1>> heads;
	private final Collection<Upstream<? extends O,K2>> tails;
	
	private final Collection<StreamSink<?,?,?>> sinks;
	private final Collection<StreamSource<?,?>> sources;

	
	private BlueprintBuilder(RosettaActionFactory actionFactory, Collection<Downstream<? super I, K1>> head, Collection<Upstream<? extends O, K2>> tails,
			Collection<StreamSink<?, ?, ?>> sinks, Collection<StreamSource<?,?>> sources) {
		this.actionFactory = actionFactory;
		this.heads = head;
		this.tails = tails;
		this.sinks = sinks;
		this.sources = sources;
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
		if (n instanceof GroupNode) {
			return (BlueprintBuilder<I, O, K1, K2>) startsWith(actionFactory, (GroupNode<I,K1, K2>)n);
		}
		if (n instanceof BlueprintInstance) {
			return startsWith(actionFactory, (BlueprintInstance<I, O, K1, K2>)n);
		}
		throw new UnsupportedOperationException("Don't know how to start a blueprint with "+n.getClass().getSimpleName());
	}
	
	public static <O, K> BlueprintBuilder<Void,O,K, K> startsWith(RosettaActionFactory actionFactory, SourceNode<O, K> sourceNode) {
		StreamSource<O,K> source = new StreamSource<>(sourceNode.getName(),sourceNode);
		return new BlueprintBuilder<>(actionFactory, Collections.emptyList(), of(source), of(), of(source));
	}
	
	public static <I, O, K> BlueprintBuilder<I,O,K, K> startsWith(RosettaActionFactory actionFactory, ExpanderNode<I,O,K> expandNode) {
		StreamExpander<I, O, K> expand = new StreamExpander<>(expandNode);
		return new BlueprintBuilder<>(actionFactory, of(expand), of(expand), of(), of());
	}
	
	public static <I, K, K2> BlueprintBuilder<I,I,K, K2> startsWith(RosettaActionFactory actionFactory, GroupNode<I,K, K2> groupNode) {
		StreamGroup<I, K, K2> group = new StreamGroup<>(groupNode);
		return new BlueprintBuilder<>(actionFactory, of(group), of(group), of(), of());
	}
	
	public static <I, O, K> BlueprintBuilder<I,O, K, K> startsWith(RosettaActionFactory actionFactory, ProcessorNode<I, O, K> processorNode) {
		StreamProcessor<I, O, K> group = new StreamProcessor<>(processorNode);
		return new BlueprintBuilder<>(actionFactory, of(group), of(group), of(), of());
	}
	
	public static <I1, I2, K> BlueprintBuilder<Object, I1, K, K> startsWith(RosettaActionFactory actionFactory, DataJoinNode<I1, I2, K> joinNode) {
		StreamDataJoin<I1, I2, K> joiner = new StreamDataJoin<>(joinNode);
		return new BlueprintBuilder<Object, I1, K, K>(actionFactory, of(joiner), of(joiner), of(), of());
	}
	
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> startsWith(RosettaActionFactory actionFactory, 
			BlueprintInstance<I, O, K1, K2> bp2) {
		return new BlueprintBuilder<>(actionFactory, Collections.singletonList(bp2), Collections.singletonList(bp2), bp2.sinks, bp2.sources);
	}
	
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> startsWith(RosettaActionFactory actionFactory, 
			BlueprintBuilder<I, O, K1, K2> bpb) {
		return bpb;
	}
	
	public <O2> BlueprintBuilder<I, O2, K1, K2> then(ExpanderNode<? super O,O2,K2> expandNode) {
		StreamExpander<O, O2, K2> expand = new StreamExpander<>(expandNode);
		//add new node downstream of existing tails
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(expand);
		}
		return new BlueprintBuilder<I, O2, K1, K2>(actionFactory, heads, of(expand), of(), sources);
	}
	
	public <K3> BlueprintBuilder<I,O, K1, K3> then(GroupNode<O,K2,K3> groupNode) {
		StreamGroup<O, K2, K3> group = new StreamGroup<>(groupNode);
		//add new node downstream of existing tails
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(group);
		}
		return new BlueprintBuilder<I,O, K1, K3>(actionFactory, heads, of(group), of(), sources);
	}
	
	public <O2> BlueprintBuilder<I, O2, K1, K2> then(ProcessorNode<? super O, O2, K2> procesorNode) {
		StreamProcessor<? super O, O2, K2> process = new StreamProcessor<>(procesorNode);
		//add new node downstream of existing tails
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(process);
		}
		return new BlueprintBuilder<I, O2, K1, K2>(actionFactory, heads, of(process), of(), sources);
	}
	
	public <I1, I2> BlueprintBuilder<I, I1, K1, K2> then(DataJoinNode<I1, I2, K2> joinNode) {
		StreamDataJoin<I1, I2, K2> joiner = new StreamDataJoin<>(joinNode);
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(joiner);
		}
		return new BlueprintBuilder<I, I1, K1, K2>(actionFactory, heads, of(joiner), of(), sources);
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
		return new BlueprintBuilder<>(actionFactory, heads, bp2.tails, sinks, sources);
	}
	
	public <O2, K3> BlueprintBuilder<I, O2, K1, K3> then(BlueprintInstance<? super O, O2, K2, K3> bp2) {
		
		//adds the blueprint as a sub graph
		tails.forEach(t->t.addDownstreams(bp2));
		Collection<StreamSink<?, ?, ?>> sinks = 
				ImmutableList.<StreamSink<?, ?, ?>>builder().addAll(this.sinks).addAll(bp2.getSinks()).build();;
		Collection<StreamSource<?,?>> sources = 
				ImmutableList.<StreamSource<?,?>>builder().addAll(this.sources).addAll(bp2.getSources()).build();
		return new BlueprintBuilder<>(actionFactory, heads, Collections.singletonList(bp2), sinks, sources);
	}
	
	public BlueprintBuilder<I,O, K1, K2> andSink(SinkNode<? super O, ?, K2> sinkNode) {
		StreamSink<? super O, ?, K2> sink = new StreamSink<>(sinkNode);
		for (Upstream<? extends O, K2> up: tails) {
			up.addDownstreams(sink);
		}
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, heads, of(), of(sink), sources);
	}
	
	@SafeVarargs
	public static <I, O, K1, K2> BlueprintBuilder<I, O, K1, K2> and(RosettaActionFactory actionFactory, BlueprintBuilder<I, ? extends O, K1, K2>... bps) {
		Collection<Downstream<? super I, K1>> heads = Arrays.stream(bps).flatMap(bp->bp.heads.stream()).collect(ImmutableList.toImmutableList());
		Collection<Upstream<? extends O, K2>> tails = Arrays.stream(bps).flatMap(bp->bp.tails.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSink<?, ?, ?>> sinks = Arrays.stream(bps).flatMap(bp->bp.sinks.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSource<?,?>> sources = Arrays.stream(bps).flatMap(bp->bp.sources.stream()).collect(ImmutableList.toImmutableList());
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, heads, tails, sinks, sources);
	}
	
	@SafeVarargs
	public static <I,O,K1, K2> BlueprintBuilder<I, O, K1, K2> ifElse(RosettaActionFactory actionFactory, String uri, String label
			, BlueprintIfThen<I, ? extends O, K1, K2>... ifThens) {
		List<BlueprintBuilder<I, ?, K1, K1>> ifs = Arrays.stream(ifThens).map(it->it.ifInstance).filter(Objects::nonNull).collect(Collectors.toList());
		List<BlueprintBuilder<I, ? extends O, K1, K2>> thens = Arrays.stream(ifThens).map(it->it.thenInstance).collect(Collectors.toList());
		List<BlueprintInstance<? super I, ?, K1, K1>> ifsB = ifs.stream().map(i->i.toBlueprint("","")).collect(Collectors.toList());
		List<BlueprintInstance<? super I, ? extends O, K1, K2>>thensB = thens.stream().map(i->i.toBlueprint("","")).collect(Collectors.toList());
		StreamOneOf<I, ? extends O, K1, K2> oneOf = new StreamOneOf<I, O, K1, K2>(uri, label, ifsB, thensB);

		for (BlueprintInstance<? super I, ? extends O, K1, K2> then:thensB) {
			then.addUpstream(null);
		}
		thens.forEach(t->t.heads.forEach(h->{}));
		Collection<Upstream<? extends O, K2>> tails = Arrays.stream(ifThens).flatMap(it->it.thenInstance.tails.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSink<?, ?, ?>> sinks = thens.stream().flatMap(bp->bp.sinks.stream()).collect(ImmutableList.toImmutableList());
		Collection<StreamSource<?,?>> sources = thens.stream().flatMap(bp->bp.sources.stream()).collect(ImmutableList.toImmutableList());
		return new BlueprintBuilder<I, O, K1, K2>(actionFactory, of (oneOf), tails, sinks, sources);
	}

	public static <I extends RosettaModelObject, K1> BlueprintInstance<I, Object, K1, K1> doSimpleMappings(RosettaActionFactory actionFactory, String uri, String label, Collection<MappingGroup<I, ?>> mappings){
		Collection<Downstream<? super I,K1>> heads = new ArrayList<>();
		Collection<Upstream<?,K1>> tails = new ArrayList<>();
		
		Collection<StreamSink<?,?,?>> sinks = Collections.emptySet();
		Collection<StreamSource<?,?>> sources = Collections.emptySet();
		
		for (MappingGroup<I, ?> mappingGroup:mappings) {
			ProcessorNode<I, ?, K1> rsm = actionFactory.newRosettaSimpleMapper(mappingGroup.getUri()+"/"+mappingGroup.getIdentifier(), mappingGroup.getIdentifier()+"/"+mappingGroup.getIdentifier(), null, mappingGroup);
			StreamProcessor<I, ?, K1> process = new StreamProcessor<>(rsm);
			heads.add(process);
			tails.add(process);
		}
		return new BlueprintInstance<I, Object, K1, K1>(uri, "Simple mapping", heads, tails, sources, sinks, null);
	}
	
	public static <I extends RosettaModelObject, O, K1> BlueprintInstance<I, O, K1, K1> doCalcMappings(RosettaActionFactory actionFactory, String uri, Collection<MappingGroup<I, ? extends O>> mappings){
		Collection<Downstream<? super I,K1>> heads = new ArrayList<>();
		Collection<Upstream<? extends O,K1>> tails = new ArrayList<>();
		
		Collection<StreamSink<?,?,?>> sinks = Collections.emptySet();
		Collection<StreamSource<?,?>> sources = Collections.emptySet();
		
		for (MappingGroup<I, ? extends O> mappingGroup:mappings) {
			ProcessorNode<I, ? extends O, K1> rsm = actionFactory.newRosettaCalculationMapper(mappingGroup.getUri(), mappingGroup.getIdentifier(), null, mappingGroup);
			StreamProcessor<I, ? extends O, K1> process = new StreamProcessor<>(rsm);
			heads.add(process);
			tails.add(process);
		}
		return new BlueprintInstance<I, O, K1, K1>(uri, "Calculation mapping", heads, tails, sources, sinks, null);
	}
	
	public BlueprintInstance<I, O, K1, K2> toBlueprint(String uri, String blueprintName) {
		return toBlueprint(uri, blueprintName, null);
	}
	
	public BlueprintInstance<I, O, K1, K2> toBlueprint(String uri, String blueprintName, ReportTypeBuilder reportBuilder) {
		return new BlueprintInstance<I, O, K1, K2>(uri, blueprintName, heads, 
				tails.stream().map(Upstream.class::cast).collect(Collectors.toList()), sources, sinks, reportBuilder);
	}
	
}
