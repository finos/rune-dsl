package com.regnosys.rosetta.blueprints.runner.data;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.blueprints.runner.nodes.NamedNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GroupableData<I, K> {

	private final K key;
	private final I data;
	private final Optional<Integer> repeatableDataIndex;
	private final DataIdentifier identifier;
	private final Collection<Issue> issues;
	private final boolean tracing;
	private final long timestamp;

	private final String nodeName;
	
	private final Collection<GroupableData<?,?>> precedents;
	
	private final Collection<GroupableData<?,?>> descendents;
	
	protected GroupableData(K key, I data, Integer repeatableDataIndex, DataIdentifier identifier, Collection<Issue> issues, NamedNode node, boolean tracing, GroupableData<?,?> precedent) {
		this.key = key;
		this.data = data;
		this.repeatableDataIndex = Optional.ofNullable(repeatableDataIndex);
		this.identifier = identifier;
		this.issues = issues;
		this.nodeName = node.getURI();
		this.tracing = tracing;
		if (tracing) {
			this.precedents = Collections.singletonList(precedent);
		}
		else {
			this.precedents = Collections.emptyList();
		}
		descendents = new ArrayList<>();
		timestamp = System.currentTimeMillis();
	}
	
	protected GroupableData(K key, I data, Integer repeatableDataIndex, DataIdentifier identifier, Collection<Issue> issues, NamedNode node, boolean tracing, Collection<GroupableData<?,?>> precedents) {
		this.key = key;
		this.data = data;
		this.repeatableDataIndex = Optional.ofNullable(repeatableDataIndex);
		this.identifier = identifier;
		this.issues = issues;
		this.nodeName = node.getURI();
		this.tracing = tracing;
		if (tracing) {
			this.precedents = ImmutableList.copyOf(precedents);
		}
		else {
			this.precedents = Collections.emptyList();
		}
		descendents = new ArrayList<>();
		timestamp = System.currentTimeMillis();
	}
	
	public static <I, K> GroupableData<I,K> initialData(K key, I data, DataIdentifier identifier, Collection<Issue> issues, NamedNode node, boolean tracing) {
		return new GroupableData<>(key, data, null, identifier, ImmutableList.copyOf(issues), node, tracing, Collections.emptyList());
	}
	
	public GroupableData<I, K> withNewIdentifier(DataIdentifier newIdentifier, Collection<Issue> newIssues, NamedNode node) {
		Collection<Issue> resultIssues = mergeIssues(newIssues);
		DataIdentifier id = newIdentifier==null?identifier:newIdentifier;
		GroupableData<I, K> groupableData = new GroupableData<>(key, data, null, id, resultIssues, node, tracing, this);
		return groupableData;
	}
	
	public <I2> GroupableData<I2,K> withIssues(I2 newData, DataIdentifier newIdentifier, Collection<Issue> newIssues, NamedNode node) {
		Collection<Issue> resultIssues = mergeIssues(newIssues);
		DataIdentifier id = newIdentifier==null?identifier:newIdentifier;
		GroupableData<I2, K> groupableData = new GroupableData<>(key, newData, null, id, resultIssues, node, tracing, this);
		descendents.add(groupableData);
		return groupableData;
	}
	
	public <K2> GroupableData<I,K2> withNewKey(K2 newKey, DataIdentifier newIdentifier, Collection<Issue> newIssues, NamedNode node) {
		Collection<Issue> resultIssues = mergeIssues(newIssues);
		DataIdentifier id = newIdentifier==null?identifier:newIdentifier;
		GroupableData<I, K2> groupableData = new GroupableData<>(newKey, data, null, id, resultIssues, node, tracing, this);
		descendents.add(groupableData);
		return groupableData;
	}
	
	public <I2> GroupableData<I2,K> withNewData(I2 newData, DataIdentifier newIdentifier, Collection<Issue> newIssues, NamedNode node) {
		Collection<Issue> resultIssues = mergeIssues(newIssues);
		DataIdentifier id = newIdentifier==null?identifier:newIdentifier;
		GroupableData<I2, K> groupableData = new GroupableData<>(key, newData, null, id, resultIssues, node, tracing, this);
		descendents.add(groupableData);
		return groupableData;
	}
	
	public <I2> GroupableData<I2,K> withNewRepeatableData(I2 newData, int index, DataIdentifier newIdentifier, Collection<Issue> newIssues, NamedNode node) {
		Collection<Issue> resultIssues = mergeIssues(newIssues);
		DataIdentifier id = newIdentifier==null?identifier:newIdentifier;
		GroupableData<I2, K> groupableData = new GroupableData<>(key, newData, index, id, resultIssues, node, tracing, this);
		descendents.add(groupableData);
		return groupableData;
	}
	
	public static <I, K> GroupableData<I,K> withMultiplePrecedents(K key, I data, DataIdentifier identifier, Collection<Issue> issues, NamedNode node, Collection<GroupableData<?,?>> precedents) {
		List<GroupableData<?,?>> tracedPrecendents = precedents.stream().filter(p->p.tracing).collect(ImmutableList.toImmutableList());
		GroupableData<I, K> groupableData = new GroupableData<>(key, data, null, identifier, ImmutableList.copyOf(issues), node, !tracedPrecendents.isEmpty(), tracedPrecendents);
		precedents.forEach(gd->gd.descendents.add(groupableData));
		return groupableData;
	}
	
	public GroupableData<I,K> withTracing(NamedNode node, boolean tracing) {
		Collection<Issue> resultIssues = issues;
		GroupableData<I, K> groupableData = new GroupableData<>(key, data, null, identifier, resultIssues, node, tracing, this);
		descendents.add(groupableData);
		return groupableData;
	}
	
	public K getKey() {
		return key;
	}

	public I getData() {
		return data;
	}
	
	public Optional<Integer> getRepeatableDataIndex() {
		return repeatableDataIndex;
	}

	public DataIdentifier getIdentifier() {
		return identifier;
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	@Override
	public String toString() {
		return "GroupableData [key=" + key + ", data=" + data + ", repeatableDataIndex=" + repeatableDataIndex + ", identifier=" + identifier + ", issues=" + issues
				+ "]";
	}

	public Collection<GroupableData<?,?>> getPrecedents() {
		return precedents;
	}
	
	public Collection<GroupableData<?,?>> getDescendents() {
		return descendents;
	}
	
	public String getNodeName() {
		return nodeName;
	}

	private Collection<Issue> mergeIssues(Collection<Issue> newIssues) {
		Collection<Issue> resultIssues = issues;
		if (!newIssues.isEmpty()) {
			resultIssues = ImmutableList.<Issue>builder().addAll(issues).addAll(newIssues).build();
		}
		return resultIssues;
	}

	public String getSummary() {
		long timeTaken=timestamp - precedents.stream().mapToLong(g->g.timestamp).max().orElse(timestamp);
		
		String pad = Strings.repeat("-", (40 - nodeName.length() - 2) / 2);
		return String.format("%s %s %s\\n \\n \\n%sms\\nkey:  %.25s\\ndata:  %.20s", pad, nodeName,pad, timeTaken, key, data);
	}
	
	
}
