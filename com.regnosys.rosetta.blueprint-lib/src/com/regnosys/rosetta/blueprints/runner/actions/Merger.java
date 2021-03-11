package com.regnosys.rosetta.blueprints.runner.actions;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.Issue;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import com.regnosys.rosetta.blueprints.runner.nodes.StatefullNode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Merger<I, B, O,K> extends StatefullNode implements ProcessorNode<I, O, K> {

	private final Function<DataIdentifier, BiConsumer<B, ? extends I>> inserters;
	private final Function<K, ? extends B> supplier;
	private final DataIdentifier resultType;
	
	private final Function<B, O> finalizer;
	
	private Map<K, IssuesAndResult<B>> results = new HashMap<>();
	
	private static class IssuesAndResult<O> {
		O result;
		Collection<Issue> issues = new ArrayList<>();
		Map<DataIdentifier, GroupableData<?,?>> precedents = new HashMap<>();
		
		public IssuesAndResult(O result) {
			this.result = result;
		}
	}
	
	public Merger(String uri, String label, Function<DataIdentifier, BiConsumer<B, ? extends I>> insertors, 
			Function<K, ? extends B> supplier, Function<B, O> finalizer,
			DataIdentifier resultType, boolean	publishIntermediate) {
		super(uri, label, publishIntermediate);
		this.inserters = insertors;
		this.supplier = supplier;
		this.resultType = resultType;
		this.finalizer = finalizer;
	}

	@Override
	public <T extends I, K2 extends K> Optional<GroupableData<O, K2>> process(GroupableData<T, K2> input) {
		IssuesAndResult<B> result = results.getOrDefault(input.getKey(), 
				new IssuesAndResult<>(supplier.apply(input.getKey())));
		@SuppressWarnings("unchecked")
		BiConsumer<B, T> inserter = (BiConsumer<B, T>) inserters.apply(input.getIdentifier());
		if (inserter!=null) {
			inserter.accept(result.result, (T)input.getData());
			result.precedents.put(input.getIdentifier(), input);
		}
		result.issues.addAll(input.getIssues());
		results.putIfAbsent(input.getKey(), result);
		if (publishIntermediate) {
			return Optional.of(input.withNewData(finalizer.apply(result.result), resultType, 
					Collections.emptyList(), this));
		}
		else return Optional.empty();
	}

	@Override
	public Collection<GroupableData<? extends O, ? extends K>> terminate() {
		return results.entrySet().stream()
				.map(e->GroupableData.withMultiplePrecedents(e.getKey(), 
						finalizer.apply(e.getValue().result), resultType, 
						e.getValue().issues, this, e.getValue().precedents.values())
					)
				.collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
	}

}
