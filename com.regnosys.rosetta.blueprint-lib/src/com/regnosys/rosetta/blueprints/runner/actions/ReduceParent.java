package com.regnosys.rosetta.blueprints.runner.actions;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ReduceParent<I, Kr extends Comparable<Kr>, K> extends NamedNode implements ProcessorNode<I, I, K>{

	protected Action action;
	protected Map<K, Candidate> candidates = new HashMap<>();

	public enum Action {
			MAXBY,
			MINBY,
			FIRST,
			LAST;
		}

	public ReduceParent(String uri, String label) {
		super(uri, label);
	}
	
	class Candidate {
		Kr key;
		GroupableData<? extends I, ? extends K> data;
		public Candidate(Kr key, GroupableData<? extends I, ? extends K> input) {
			this.key = key;
			this.data = input;
		}
	}

	protected Candidate mergeMax(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		if (rival.key==null) return incumbant;
		else if (rival.key.compareTo(incumbant.key)>0) {
			return rival;
		}
		return incumbant;
	}

	protected Candidate mergeMin(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		if (rival.key==null) return incumbant;
		else if (rival.key.compareTo(incumbant.key)<0) {
			return rival;
		}
		return incumbant;
	}

	protected Candidate first(Candidate incumbant, Candidate rival) {
		if (incumbant==null) return rival;
		return incumbant;
	}

	protected Candidate last(Candidate incumbant, Candidate rival) {
		return rival;
	}

	@Override
	public <T extends I, K2 extends K> Optional<GroupableData<I, K2>> process(GroupableData<T, K2> input) {
		Kr key = getReduction(input);
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

	protected abstract <T extends I, K2 extends K>  Kr getReduction(GroupableData<T, K2> input);

	@Override
	public Collection<GroupableData<? extends I, ? extends K>> terminate() {
		Function<Candidate, GroupableData<? extends I, ? extends K>> f = c->c.data.withIssues(c.data.getData(), Collections.emptyList(), this);
		return candidates.values().stream().map(f).collect(Collectors.toList());
	}

}