package com.regnosys.rosetta.lib.labelprovider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A representation of a node in a directed graph for finding labels.
 * 
 * Each node has a map of labels for a given path. If a given input matches one of the paths
 * in the node, a label is found.
 * 
 * Each directed edge has a path element which is consumed when traversing the edge.
 */
public class LabelNode {
	private final Map<List<String>, String> labels = new HashMap<>();
	private final Map<String, LabelNode> outgoingEdges = new HashMap<>();
	
	public void addLabel(List<String> path, String label) {
		labels.put(path, label);
	}
	public void addOutgoingEdge(String pathElement, LabelNode connectedNode) {
		outgoingEdges.put(pathElement, connectedNode);
	}
	
	public String findLabelByTraversingGraph(LinkedList<String> inputPath) {
		if (inputPath.isEmpty()) {
			return null;
		}
		String labelFromNode = getLabelFromCurrentNode(inputPath);
		if (labelFromNode != null) {
			return labelFromNode;
		}
		String consumedInput = inputPath.poll();
		LabelNode nextNode = findNextNode(consumedInput);
		if (nextNode == null) {
			return null;
		}
		return nextNode.findLabelByTraversingGraph(inputPath);
	}
	
	private LabelNode findNextNode(String consumedInput) {
		return outgoingEdges.get(consumedInput);
	}
	private String getLabelFromCurrentNode(List<String> path) {
		return labels.get(path);
	}
}
