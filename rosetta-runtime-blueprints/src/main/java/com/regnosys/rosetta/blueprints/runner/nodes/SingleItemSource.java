/*
 * Copyright (c) 2022. REGnosys LTD
 * All rights reserved.
 */

package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.DataItemIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collections;
import java.util.Optional;

public class SingleItemSource<T> extends NamedNode implements SourceNode<T, String> {
	private T item = null;

	public SingleItemSource(String uri, String label, DataItemIdentifier identifier) {
		super(uri, label, identifier);
	}
	
	public void setItem(T item) {
		this.item = item;
	}

	@Override
	public Optional<GroupableData<T, String>> nextItem() {
		if (item != null) {
			GroupableData<T, String> result = GroupableData.initialData(getLabel(), item, getIdentifier(), Collections.emptyList(), this, false);
			item = null;
			return Optional.of(result);
        }
        return Optional.empty();
	}

}
