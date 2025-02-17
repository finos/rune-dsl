package com.regnosys.rosetta.lib.labelprovider;

import java.util.LinkedList;
import java.util.stream.Collectors;

import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.path.RosettaPath.Element;

public class GraphBasedLabelProvider implements LabelProvider {
	protected final LabelNode startNode;
	
	public GraphBasedLabelProvider(LabelNode startNode) {
		this.startNode = startNode;
	}

	@Override
	public String getLabel(RosettaPath path) {
		// Use LinkedList to process the path elements since we need to remove
		// the first element many times, which is O(1) for linked lists, but O(n) for an array list.
		LinkedList<String> pathElements = path.allElements().stream()
				.map(Element::getPath)
				.collect(Collectors.toCollection(LinkedList::new));
		return startNode.findLabelByTraversingGraph(pathElements);
	}
}
