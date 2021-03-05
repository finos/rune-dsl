package com.rosetta.model.lib.functions;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperGroupBy;
import com.rosetta.model.lib.mapper.MapperS;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class MapperGroupByTest {

	private final static ObjectBranchNode OBJECT_BRANCH_NODE_1 = new ObjectBranchNode("A", 4);
	private final static ObjectBranchNode OBJECT_BRANCH_NODE_2 = new ObjectBranchNode("A", 2);
	private final static ObjectBranchNode OBJECT_BRANCH_NODE_3 = new ObjectBranchNode("B", 3);
	private final static ObjectBranchNode OBJECT_BRANCH_NODE_4 = new ObjectBranchNode("B", 1);
	private final static ListBranchNode LIST_BRANCH_NODE_1 = new ListBranchNode(OBJECT_BRANCH_NODE_1);
	private final static ListBranchNode LIST_BRANCH_NODE_2 = new ListBranchNode(OBJECT_BRANCH_NODE_2);
	private final static ListBranchNode LIST_BRANCH_NODE_3 = new ListBranchNode(OBJECT_BRANCH_NODE_3);
	private final static ListBranchNode LIST_BRANCH_NODE_4 = new ListBranchNode(OBJECT_BRANCH_NODE_4);
	private final static ListBranchNode LIST_BRANCH_NODE_5 = new ListBranchNode(null);
	private final static ListBranchNode LIST_BRANCH_NODE_NULL = new ListBranchNode(null);
	
	@Test
	public void testListBranchNodeMapping() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3, LIST_BRANCH_NODE_4, LIST_BRANCH_NODE_5, LIST_BRANCH_NODE_NULL));
		MapperGroupBy<Integer, String> mapperGroupBy = MapperS.of(foo)
				.mapC("getListBranchNodes", Foo::getListBranchNodes)
				.map("getObjectBranchNodes", ListBranchNode::getObjectBranchNodes)
				.groupBy(i->new MapperS<>(i).map("getStringLeafNode", ObjectBranchNode::getStringLeafNode))
				.map("getIntLeafNode", ObjectBranchNode::getIntLeafNode);

		
		Map<MapperS<String>, Mapper<Integer>> mappersMap = mapperGroupBy.getGroups();
		
		// A
		
		Mapper<Integer> aMapper = mappersMap.get(MapperS.of("A"));
		assertNotNull(aMapper);
		
		List<String> aPaths = aMapper.getPaths().stream().map(Mapper.Path::getFullPath).collect(Collectors.toList());
		assertThat("Unexpected number of paths", aPaths.size(), is(2));
		assertThat(aPaths, hasItems(
				"Foo->getListBranchNodes[0]->getObjectBranchNodes->getIntLeafNode",
				"Foo->getListBranchNodes[1]->getObjectBranchNodes->getIntLeafNode"));
		
		assertThat("Unexpected number of error paths", aMapper.getErrorPaths().size(), is(0));
		
		assertNull(aMapper.get(), "Expected null leafNode object because more than 1 element");
		
		List<Integer> aMulti = aMapper.getMulti();
		assertThat("Unexpected number of multi", aMulti.size(), is(2));
		assertThat(aMulti, hasItems(4, 2));
		
		assertFalse(aMapper.getParent().isPresent(), "Expected absent parent object because more than 1 element");
		
		@SuppressWarnings("unchecked")
		List<ObjectBranchNode> aParentMulti = (List<ObjectBranchNode>) aMapper.getParentMulti();
		assertThat("Unexpected number of multiParent", aParentMulti.size(), is(2));
		assertThat(aParentMulti, hasItems(OBJECT_BRANCH_NODE_1, OBJECT_BRANCH_NODE_2));
		
		assertThat(aMapper.getErrors().isEmpty(), is(true));
		
		// B
		
		Mapper<Integer> bMapper = mappersMap.get(MapperS.of("B"));
		assertNotNull(bMapper);
		
		List<String> bPaths = bMapper.getPaths().stream().map(Mapper.Path::getFullPath).collect(Collectors.toList());
		assertThat("Unexpected number of paths", bPaths.size(), is(2));
		assertThat(bPaths, hasItems(
				"Foo->getListBranchNodes[2]->getObjectBranchNodes->getIntLeafNode",
				"Foo->getListBranchNodes[3]->getObjectBranchNodes->getIntLeafNode"));
		
		assertThat("Unexpected number of error paths", bMapper.getErrorPaths().size(), is(0));
		
		assertNull(bMapper.get(), "Expected null leafNode object because more than 1 element");
		
		List<Integer> bMulti = bMapper.getMulti();
		assertThat("Unexpected number of multi", bMulti.size(), is(2));
		assertThat(bMulti, hasItems(3, 1));
		
		assertFalse(bMapper.getParent().isPresent(), "Expected absent parent object because more than 1 element");
		
		@SuppressWarnings("unchecked")
		List<ObjectBranchNode> bParentMulti = (List<ObjectBranchNode>) bMapper.getParentMulti();
		assertThat("Unexpected number of multiParent", bParentMulti.size(), is(2));
		assertThat(bParentMulti, hasItems(OBJECT_BRANCH_NODE_3, OBJECT_BRANCH_NODE_4));
		
		assertThat(bMapper.getErrors().isEmpty(), is(true));
	}
	
	private static class Foo {
		private final List<ListBranchNode> listBranchNodes;
		
		public Foo(List<ListBranchNode> listBranchNodes) {
			this.listBranchNodes = listBranchNodes;
		}

		public List<ListBranchNode> getListBranchNodes() {
			return listBranchNodes;
		}
	}
	
	private static class ListBranchNode {
		private final ObjectBranchNode objectBranchNodes;
		
		public ListBranchNode(ObjectBranchNode objectBranchNodes) {
			this.objectBranchNodes = objectBranchNodes;
		}

		public ObjectBranchNode getObjectBranchNodes() {
			return objectBranchNodes;
		}
	}
	
	private static class ObjectBranchNode {
		private final String stringLeafNode;
		private final Integer intLeafNode;
		
		public ObjectBranchNode(String stringLeafNode, Integer intLeafNode) {
			this.stringLeafNode = stringLeafNode;
			this.intLeafNode = intLeafNode;
		}

		public String getStringLeafNode() {
			return stringLeafNode;
		}

		public Integer getIntLeafNode() {
			return intLeafNode;
		}
	}
}
