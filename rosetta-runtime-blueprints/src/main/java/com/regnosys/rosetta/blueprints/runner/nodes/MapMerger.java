/*
 * Copyright (c) 2022. REGnosys LTD
 * All rights reserved.
 */

package com.regnosys.rosetta.blueprints.runner.nodes;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.DataItemIdentifier;
import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author TomForwood
 *
 * @param <K>
 * For each input key produces a Map of key value pairs where the keys in the map are the DataIdentifiers of pieces of data and the values are the pieces of data
 */
public class MapMerger<K> extends StatefullNode implements ExpanderNode<Object, Map<DataIdentifier, GroupableData<?, ? extends K>>, K> {

	private final Table<K, DataIdentifier, GroupableData<?, ? extends K>> table;
	
	public MapMerger(String uri, String label, boolean intermediate, DataIdentifier identifier) {
		super(uri, label, intermediate, identifier);
		table = HashBasedTable.create();
	}

	@Override
	public <T, KO extends K> Collection<GroupableData<? extends Map<DataIdentifier, GroupableData<?, ? extends K>>, KO>> process(GroupableData<T, KO> input) {
		K key = input.getKey();
		DataIdentifier fieldId = input.getIdentifier();
		if (!table.contains(key, getIdentifier())) {
			GroupableData<String, K> data = GroupableData.initialData(key, key.toString(), getIdentifier(),
				input.getIssues(), this, false);
			table.put(key, getIdentifier(), data);
		}
		if (fieldId != null) {
			table.put(key, fieldId, input);
		}
		return Collections.emptyList();
	}

	@Override
	public Collection<GroupableData<? extends Map<DataIdentifier, GroupableData<?, ? extends K>>, ? extends K>> terminate() {
		return table.rowMap().entrySet().stream()
					.map(this::initial)
					.collect(Collectors.toList());
	}
	
	GroupableData<Map<DataIdentifier, GroupableData<?, ? extends K>>, ? extends K> initial(Entry<K, Map<DataIdentifier, GroupableData<?, ? extends K>>> e) {
		return GroupableData.initialData(e.getKey(), e.getValue(), new DataItemIdentifier("row"), Collections.emptyList(), this, false);
	}
}
