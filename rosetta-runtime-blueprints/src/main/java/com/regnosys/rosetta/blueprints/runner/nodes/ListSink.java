/*
 * Copyright (c) 2022. REGnosys LTD
 * All rights reserved.
 */

package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.Issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class ListSink<I, K> extends NamedNode implements SinkNode<I, List<I>, K> {

	public ListSink(String uri, String label, DataIdentifier identifier) {
		super(uri, label, identifier);
	}

	private List<I> result = new ArrayList<>();
	private Collection<GroupableData<? extends I, ? extends K>> finalData = new ArrayList<>();
	List<Collection<Issue>> issues = new ArrayList<>();
	private FutureResult<List<I>> resultFuture = new FutureResult<>();
	private FutureResult<List<Collection<Issue>>> issuesFuture = new FutureResult<>();
	
	@Override
	public void process(GroupableData<? extends I, ? extends K> input) {
		result.add(input.getData());
		issues.add(input.getIssues());
		finalData.add(input);
	}

	@Override
	public void terminate() {
		resultFuture.postResult(result);
		issuesFuture.postResult(issues);
	}

	@Override
	public Future<List<I>> result() {
		return resultFuture;
	}

	@Override
	public Future<List<Collection<Issue>>> issues() {
		return issuesFuture;
	}

	public Collection<GroupableData<? extends I, ? extends K>> getFinalData() {
		return finalData;
	}

}
