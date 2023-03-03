package com.rosetta.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestBreadthFirstSearch {

	@Test
	void testMostBasic() {
		Node testGraph = new Node(1,new Node(2), new Node(3));
		
		List<Node> search = BreadthFirstSearch.search(testGraph, t->t.children, t->t.value==3);
		assertEquals("[1, 3]", search.toString());
	}
	
	@Test
	void testActuallyBreadthFirst() {
		Node testGraph = new Node(1,new Node(2, new Node(4, new Node(9)), new Node(3)), new Node(3));
		
		List<Node> search = BreadthFirstSearch.search(testGraph, t->t.children, t->t.value==3);
		assertEquals("[1, 3]", search.toString());
	}
	
	@Test
	void testLoops() {
		Node threeNode = new Node(3);
		Node testGraph = new Node(1,new Node(2, new Node(4, new Node(9)), threeNode), threeNode);
		threeNode.addChild(testGraph);
		
		List<Node> search = BreadthFirstSearch.search(testGraph, t->t.children, t->t.value==5);
		assertNull(search);
	}
	
	class Node {
		int value;
		List<Node> children;
		
		public Node(int val) {
			this.value = val;
			children = new ArrayList<>();
		}
		public Node(int val, Node...nodes) {
			this.value = val;
			this.children = Arrays.asList(nodes);
		}
		
		public void addChild(Node n) {
			this.children.add(n);
		}
		
		public String toString() {
			return Integer.toString(value);
		}
	}

}
