package com.rosetta.model.lib.expression;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperators;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.mapper.MapperTree;

public class ExpressionOperatorsMapperTreeTest {

	private final static BranchNode BRANCH_NODE_1 = new BranchNode(5);
	private final static BranchNode BRANCH_NODE_2 = new BranchNode(5);
	private final static BranchNode BRANCH_NODE_3 = new BranchNode(10);
	private final static BranchNode BRANCH_NODE_NULL = new BranchNode(null);
	
	@Test
	public void shouldCompareEqualListsAndReturnSuccess() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_2), null);
		
		MapperTree<Integer> tree1 = MapperTree.of(MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> tree2 = MapperTree.of(MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(tree1, tree2);
		
		assertThat(result.get(), is(true));
	}
	
	@Test
	public void shouldCompareDifferentSizeListsAndReturnFail() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_2), null);
		
		MapperTree<Integer> tree1 = MapperTree.of(MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> tree2 = MapperTree.of(MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(tree1, tree2);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] cannot be compared to [Foo->getListBranchNodes[0]->getIntLeafNode, Foo->getListBranchNodes[1]->getIntLeafNode] [5, 5]"));
	}
	
	@Test
	public void shouldCompareEqualListLeavesAndObjectLeafAndReturnSuccess() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_2), BRANCH_NODE_1);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(true));
	}
	
	@Test
	public void shouldCompareUnequalListLeafAndObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_3), BRANCH_NODE_1);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [10] does not equal [Foo->getObjectBranchNode->getIntLeafNode] [5]"));
	}
	
	@Test
	public void shouldCompareUnequalListLeavesAndObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_3), BRANCH_NODE_1);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode, Foo->getListBranchNodes[1]->getIntLeafNode] [5, 10] does not equal [Foo->getObjectBranchNode->getIntLeafNode] [5]"));
	}
	
	@Test
	public void shouldCompareListLeafAndNullObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1), BRANCH_NODE_NULL);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] does not equal [Foo->getObjectBranchNode->getIntLeafNode]"));
	}
	
	@Test
	public void shouldCompareNullListLeafAndNullObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_NULL), BRANCH_NODE_NULL);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] cannot be compared to [Foo->getObjectBranchNode->getIntLeafNode]"));
	}
	
	@Test
	public void shouldCompareListLeafAndNullObjectBranchAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] does not equal [Foo->getObjectBranchNode]"));
	}
	
	@Test
	public void shouldCompareEmptyListAndNullObjectBranchAndReturnFail() {
		Foo foo = new Foo(Collections.emptyList(), null);
		
		MapperTree<Integer> treeC = MapperTree.of(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode));
		MapperTree<Integer> treeS = MapperTree.of(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode));
		
		ComparisonResult result = ExpressionOperators.areEqual(treeC, treeS);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes] cannot be compared to [Foo->getObjectBranchNode]"));
	}
	
	@Test
	public void shouldCompareEqualMapperTreeWithOrAndReturnSuccess() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_3), null);
		Foo foo3 = new Foo(Arrays.asList(BRANCH_NODE_2), null);
		Foo foo4 = new Foo(Arrays.asList(BRANCH_NODE_NULL), null);
		
		MapperTree<Integer> tree1 = MapperTree.or(
				MapperTree.of(MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)),
				MapperTree.of(MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)));
		MapperTree<Integer> tree2 = MapperTree.or(
				MapperTree.of(MapperS.of(foo3).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)),
				MapperTree.of(MapperS.of(foo4).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)));
		ComparisonResult result = ExpressionOperators.areEqual(tree1, tree2);
		
		assertThat(result.get(), is(true));
	}
	
	@Test
	public void shouldCompareEqualMapperTreesWithAndAndReturnFail() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_3), null);
		Foo foo3 = new Foo(Arrays.asList(BRANCH_NODE_2), null);
		Foo foo4 = new Foo(Arrays.asList(BRANCH_NODE_NULL), null);
		
		MapperTree<Integer> tree1 = MapperTree.and(
				MapperTree.of(MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)),
				MapperTree.of(MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)));
		MapperTree<Integer> tree2 = MapperTree.and(
				MapperTree.of(MapperS.of(foo3).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)),
				MapperTree.of(MapperS.of(foo4).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode)));
		ComparisonResult result = ExpressionOperators.areEqual(tree1, tree2);
		
		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [10] does not equal [Foo->getListBranchNodes[0]->getIntLeafNode] [5]"));
	}
	
	// Test classes
	
	private static class Foo {
		private final List<BranchNode> listBranchNodes;
		private final BranchNode objectBranchNode;
		
		public Foo(List<BranchNode> listBranchNodes, BranchNode objectBranchNode) {
			this.listBranchNodes = listBranchNodes;
			this.objectBranchNode = objectBranchNode;
		}

		public List<BranchNode> getListBranchNodes() {
			return listBranchNodes;
		}

		public BranchNode getObjectBranchNode() {
			return objectBranchNode;
		}
	}
		
	private static class BranchNode {
		private final Integer intLeafNode;
		
		public BranchNode(Integer intLeafNode) {
			this.intLeafNode = intLeafNode;
		}

		public Integer getIntLeafNode() {
			return intLeafNode;
		}
	}
}
