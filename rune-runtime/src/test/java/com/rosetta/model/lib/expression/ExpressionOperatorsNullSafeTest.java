/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.expression;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ExpressionOperatorsNullSafeTest {

	private final static BranchNode BRANCH_NODE_1 = new BranchNode(5);
	private final static BranchNode BRANCH_NODE_2 = new BranchNode(5);
	private final static BranchNode BRANCH_NODE_3 = new BranchNode(10);
	private final static BranchNode BRANCH_NODE_NULL = new BranchNode(null);

	@Test
	public void shouldCompareEqualListsAndReturnSuccess() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_2), null);

		Mapper<Integer> mapper1 = MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapper2 = MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapper1, mapper2, CardinalityOperator.All);

		assertThat(result.get(), is(true));
	}

	@Test
	public void shouldCompareDifferentSizeListsAndReturnFail() {
		Foo foo1 = new Foo(Arrays.asList(BRANCH_NODE_1), null);
		Foo foo2 = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_2), null);

		Mapper<Integer> mapper1 = MapperS.of(foo1).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapper2 = MapperS.of(foo2).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapper1, mapper2, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] cannot be compared to [Foo->getListBranchNodes[0]->getIntLeafNode, Foo->getListBranchNodes[1]->getIntLeafNode] [5, 5]"));
	}

	@Test
	public void shouldCompareEqualListLeavesAndObjectLeafAndReturnSuccess() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_2), BRANCH_NODE_1);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(true));
	}

	@Test
	public void shouldCompareUnequalListLeafAndObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_3), BRANCH_NODE_1);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [10] does not equal [Foo->getObjectBranchNode->getIntLeafNode] [5]"));
	}

	@Test
	public void shouldCompareUnequalListLeavesAndObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1, BRANCH_NODE_3), BRANCH_NODE_1);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode, Foo->getListBranchNodes[1]->getIntLeafNode] [5, 10] does not equal [Foo->getObjectBranchNode->getIntLeafNode] [5]"));
	}

	@Test
	public void shouldCompareListLeafAndNullObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1), BRANCH_NODE_NULL);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] does not equal [Foo->getObjectBranchNode->getIntLeafNode]"));
	}

	@Test
	public void shouldCompareNullListLeafAndNullObjectLeafAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_NULL), BRANCH_NODE_NULL);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] cannot be compared to [Foo->getObjectBranchNode->getIntLeafNode]"));
	}

	@Test
	public void shouldCompareListLeafAndNullObjectBranchAndReturnFail() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1), null);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes[0]->getIntLeafNode] [5] does not equal [Foo->getObjectBranchNode]"));
	}

	@Test
	public void shouldCompareEmptyListAndNullObjectBranchAndReturnFail() {
		Foo foo = new Foo(Collections.emptyList(), null);

		Mapper<Integer> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", BranchNode::getIntLeafNode);
		Mapper<Integer> mapperS = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", BranchNode::getIntLeafNode);

		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(mapperC, mapperS, CardinalityOperator.All);

		assertThat(result.get(), is(false));
		assertThat(result.getError(), is("[Foo->getListBranchNodes] cannot be compared to [Foo->getObjectBranchNode]"));
	}


	@Test
	public void containsTest() {
		Foo foo = new Foo(Arrays.asList(BRANCH_NODE_1,BRANCH_NODE_2), null);

		Mapper<BranchNode> mapperC = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes);

		ComparisonResult result = ExpressionOperatorsNullSafe.contains(mapperC, MapperC.of(MapperS.of(BRANCH_NODE_1), MapperS.of(BRANCH_NODE_2)));
		assertThat(result.get(), is(true));

		result = ExpressionOperatorsNullSafe.contains(mapperC, MapperS.of(BRANCH_NODE_3));
		assertThat(result.get(), is(false));

		result = ExpressionOperatorsNullSafe.contains(mapperC, MapperC.of(MapperS.of(BRANCH_NODE_1), MapperS.of(BRANCH_NODE_3)));
		assertThat(result.get(), is(false));

		assertThat(result.getError(), is("[5, 5] does not contain all of [5, 10]"));
	}

	@Test
	public void countElementsListLiteral() {
		ComparisonResult result = ExpressionOperatorsNullSafe.areEqual(
				MapperS.of(MapperC.of(MapperS.of(BRANCH_NODE_1), MapperS.of(BRANCH_NODE_3)).resultCount()), MapperS.of(Integer.valueOf(2)),
				CardinalityOperator.All);
		assertThat(result.get(), is(true));
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

		@Override
		public String toString() {
			return intLeafNode == null?"null":String.valueOf(intLeafNode);
		}
	}
}
