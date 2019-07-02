package com.regnosys.rosetta.blueprints.runner.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;

public class ReduceBy<I, Kr extends Comparable<Kr> , K extends Comparable<K>> extends NamedNode implements ProcessorNode<I, I, K> {

	private Action action;

	Map<K, Candidate> candidates=new HashMap<>();

	private Function<? super I, Kr> evalFunc;
	
	public ReduceBy(String uri, String label, Action action, Function<I, Kr> evalFunc) {
		super(uri,label);
		this.action = action;
		this.evalFunc = evalFunc;
	}
	public ReduceBy(String uri, String label, Action action) {
		super(uri,label);
		this.action = action;
		evalFunc = a->null;
	}

	public enum Action {
		MAXBY,
		MINBY,
		FIRST,
		LAST;
	}

	@Override
	public <T extends I> Optional<GroupableData<I, K>> process(GroupableData<T, K> input) {
		Kr key = evalFunc.apply(input.getData());
		K group = input.getKey();
		Candidate rival = new Candidate(key, input);
		switch (action) {
		case MAXBY:
			candidates.merge(group, rival, this::mergeMax);
			break;
		case MINBY:
			candidates.merge(group, rival, this::mergeMin);
			break;
		case FIRST :
			candidates.merge(group, rival, this::first);
			break;
		case LAST :
			candidates.merge(group, rival, this::last);	
			break;
		}
		return Optional.empty();
	}
	
	@Override
	public Collection<GroupableData<? extends I, K>> terminate() {
		Function<Candidate, GroupableData<? extends I, K>> f = c->c.data.withIssues(c.data.getData(), Collections.emptyList(), this);
		return candidates.values().stream().map(f).collect(Collectors.toList());
	}
	
	private Candidate mergeMax(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		if (rival.key==null) return incumbant;
		else if (rival.key.compareTo(incumbant.key)>0) {
			return rival;
		}
		return incumbant;
	}
	
	private Candidate mergeMin(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		if (rival.key==null) return incumbant;
		else if (rival.key.compareTo(incumbant.key)<0) {
			return rival;
		}
		return incumbant;
	}
	private Candidate first(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		return incumbant;
	}
	private Candidate last(Candidate incumbant, Candidate rival) {
		return rival;
	}
	
	private class Candidate {
		Kr key;
		private GroupableData<? extends I, K> data;
		public Candidate(Kr key, GroupableData<? extends I, K> input) {
			this.key = key;
			this.data = input;
		}
	}

}
