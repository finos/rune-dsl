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

package com.rosetta.model.lib.functions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;

public class MapperTest {

	private final static ObjectBranchNode OBJECT_BRANCH_NODE = new ObjectBranchNode("A", 4);
	private final static ListBranchNode LIST_BRANCH_NODE_1 = new ListBranchNode(1, Arrays.asList(OBJECT_BRANCH_NODE));
	private final static ListBranchNode LIST_BRANCH_NODE_2 = new ListBranchNode(2, null);
	private final static ListBranchNode LIST_BRANCH_NODE_3 = new ListBranchNode(3, Collections.emptyList());
	private final static ListBranchNode LIST_BRANCH_NODE_NULL = new ListBranchNode(null, null);
	private final static ObjectBranchNode OBJECT_BRANCH_NODE_NULL = new ObjectBranchNode(null, null);
	
	@Test
	public void testListBranchNodeMappingSuccess() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);
		
		Mapper<Integer> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode);
		
		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(3));
		assertTrue(paths.contains("Foo->getListBranchNodes[0]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[1]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[2]->getIntLeafNode"), "Expected mapper paths not found");
		
		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));
		
		Integer leafNodeObject = mapper.get();
		assertNull(leafNodeObject, "Expected null leafNode object because more than 1 element");
		
		List<Integer> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(3));
		assertTrue(multi.contains(1), "Expected mapper multi values not found");
		assertTrue(multi.contains(2), "Expected mapper multi values not found");
		assertTrue(multi.contains(3), "Expected mapper multi values not found");
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertFalse(parentLeafNodeObject.isPresent(), "Expected absent parent object because more than 1 element");
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(3));
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_1), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_2), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_3), "Expected mapper multi parent object not found");
		
		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	@Test
	public void testListToListBranchNodeMappingSuccess() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);
		
		Mapper<String> mapper = MapperS.of(foo)
				.mapC("getListBranchNodes", Foo::getListBranchNodes)
				.mapC("getObjectBranchNodes", ListBranchNode::getObjectBranchNodes)
				.map("getStringLeafNode", ObjectBranchNode::getStringLeafNode);
		
		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(1));
		assertThat(paths.get(0), is("Foo->getListBranchNodes[0]->getObjectBranchNodes[0]->getStringLeafNode"));
		
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", errorPaths.size(), is(2));
		assertThat(errorPaths.get(0), is("Foo->getListBranchNodes[1]->getObjectBranchNodes"));
		assertThat(errorPaths.get(1), is("Foo->getListBranchNodes[2]->getObjectBranchNodes"));
		
		String leafNodeObject = mapper.get();
		assertThat("Expected mapper leaf node object did not match", leafNodeObject, is("A"));
		
		List<String> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(1));
		assertThat("Expected mapper multi values not found", multi.get(0), is("A"));
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertTrue(parentLeafNodeObject.isPresent(), "Expected present parent object");
		assertThat("Expected mapper parent did not match", parentLeafNodeObject.get(), is(OBJECT_BRANCH_NODE));
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(1));
		assertThat("Expected mapper multi parent object did not match", parentMulti.get(0), is(OBJECT_BRANCH_NODE));
				
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(2));
		assertThat("Unexpected error message", errors.get(0), is("[Foo->getListBranchNodes[1]->getObjectBranchNodes] is not set"));
		assertThat("Unexpected error message", errors.get(1), is("[Foo->getListBranchNodes[2]->getObjectBranchNodes] is not set"));
	}
	
	@Test
	public void testObjectBranchNodeMappingSuccess() {
		Foo foo = new Foo(Collections.emptyList(), OBJECT_BRANCH_NODE);
		
		Mapper<String> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getStringLeafNode", ObjectBranchNode::getStringLeafNode);
		
		List<String> path = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", path.size(), is(1));
		assertTrue(path.contains("Foo->getObjectBranchNode->getStringLeafNode"), "Expected mapper paths not found");
		
		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));
		
		String leafNodeObject = mapper.get();
		assertThat("Expected mapper leaf node object did not match", leafNodeObject, is("A"));
		
		List<String> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(1));
		assertTrue(multi.contains("A"), "Expected mapper multi values not found");
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertTrue(parentLeafNodeObject.isPresent(), "Expected present parent object");
		assertThat("Expected mapper parent did not match", parentLeafNodeObject.get(), is(OBJECT_BRANCH_NODE));
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(1));
		assertTrue(parentMulti.contains(OBJECT_BRANCH_NODE), "Expected mapper multi parent object not found");
		
		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	// Object errors
	
	@Test
	public void testObjectBranchNodeMappingErrorOnNullLeafNode() {
		Foo foo = new Foo(Collections.emptyList(), OBJECT_BRANCH_NODE_NULL);
		
		Mapper<String> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getStringLeafNode", ObjectBranchNode::getStringLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
		
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of error paths", errorPaths.size(), is(1));
		assertTrue(errorPaths.contains("Foo->getObjectBranchNode->getStringLeafNode"), "Expected mapper error paths not found");
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Expected zero multi objects because of error", mapper.getMulti().size(), is(0));
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertTrue(parentLeafNodeObject.isPresent(), "Expected present parent object");
		assertThat("Expected mapper parent did not match", parentLeafNodeObject.get(), is(OBJECT_BRANCH_NODE_NULL));
		
		assertThat("Unexpected zero multiParent objects because of error", mapper.getParentMulti().size(), is(1));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("Foo->getObjectBranchNode->getStringLeafNode was null"));
	}
	
	@Test
	public void testObjectBranchNodeMappingErrorOnNullBranchNode() {
		Foo foo = new Foo(Collections.emptyList(), null);
		
		Mapper<String> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getStringLeafNode", ObjectBranchNode::getStringLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
				
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of error paths", errorPaths.size(), is(1));
		assertTrue(errorPaths.contains("Foo->getObjectBranchNode"), "Expected mapper error paths not found");
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Expected zero multi objects because of error", mapper.getMulti().size(), is(0));
		assertFalse(mapper.getParent().isPresent(), "Expected absent parent object");
		assertThat("Expected zero multiParent objects because of error", mapper.getParentMulti().size(), is(0));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("Foo->getObjectBranchNode was null"));
	}
	
	@Test
	public void testObjectBranchNodeMappingErrorOnNullRootNode() {
		Foo foo = new Foo(null, null);
		
		Mapper<String> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getStringLeafNode", ObjectBranchNode::getStringLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
				
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", errorPaths.size(), is(1));
		assertTrue(errorPaths.contains("Foo->getObjectBranchNode"), "Expected mapper paths not found");
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Unexpected zero multi objects because of error", mapper.getMulti().size(), is(0));
		assertFalse(mapper.getParent().isPresent(), "Expected absent parent object because of error");
		assertThat("Unexpected zero multiParent objects because of error", mapper.getParentMulti().size(), is(0));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("Foo->getObjectBranchNode was null"));
	}
	
	// List errors
	
	@Test
	public void testListBranchNodeMappingErrorOnNullLeafNode() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_NULL), OBJECT_BRANCH_NODE);
		
		Mapper<Integer> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
				
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of error paths", errorPaths.size(), is(1));
		assertThat("Expected mapper error paths not found", errorPaths.get(0), is("Foo->getListBranchNodes[0]->getIntLeafNode"));
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Unexpected zero multi objects because of error", mapper.getMulti().size(), is(0));
		assertFalse(mapper.getParent().isPresent(), "Expected absent parent object because of error");
		assertThat("Unexpected zero multiParent objects because of error", mapper.getParentMulti().size(), is(0));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("[Foo->getListBranchNodes[0]->getIntLeafNode] is not set"));
	}
	
	@Test
	public void testListBranchNodeMappingErrorOnNullBranchNode() {
		Foo foo = new Foo(Collections.emptyList(), OBJECT_BRANCH_NODE);
		
		Mapper<Integer> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
		
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of error paths", errorPaths.size(), is(1));
		assertThat("Expected mapper error paths not found", errorPaths.get(0), is("Foo->getListBranchNodes"));
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Expected zero multi objects because of error", mapper.getMulti().size(), is(0));
		assertFalse(mapper.getParent().isPresent(), "Expected absent parent object because of error");
		assertThat("Expected zero multiParent objects because of error", mapper.getParentMulti().size(), is(0));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("[Foo->getListBranchNodes] is not set"));
	}
	
	@Test
	public void testListBranchNodeMappingErrorOnNullRootNode() {
		Foo foo = new Foo(null, null);
		
		Mapper<Integer> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode);
		
		assertThat("Unexpected number of paths", mapper.getPaths().size(), is(0));
		
		List<String> errorPaths = mapper.getErrorPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of error paths", errorPaths.size(), is(1));
		assertThat("Expected mapper error paths not found", errorPaths.get(0), is("Foo->getListBranchNodes"));
		
		assertNull(mapper.get(), "Expected null leafNode object because of error");
		assertThat("Unexpected zero multi objects because of error", mapper.getMulti().size(), is(0));
		assertFalse(mapper.getParent().isPresent(), "Expected absent parent object because of error");
		assertThat("Unexpected zero multiParent objects because of error", mapper.getParentMulti().size(), is(0));
		
		List<String> errors = mapper.getErrors();
		assertThat("Unexpected number of errors", errors.size(), is(1));
		assertThat("Unexpected error message", errors.get(0), is("[Foo->getListBranchNodes] is not set"));
	}
	
	@Test
	public void testListBranchNodeMappingSuccessWithUnionSame() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);
		
		Mapper<Integer> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode)
				.unionSame(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", ObjectBranchNode::getIntLeafNode));
		
		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(4));
		assertTrue(paths.contains("Foo->getListBranchNodes[0]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[1]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[2]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getObjectBranchNode->getIntLeafNode"), "Expected mapper paths not found");
		
		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));
		
		Object leafNodeObject = mapper.get();
		assertNull(leafNodeObject, "Expected null leafNode object because more than 1 element");
		
		List<Integer> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(4));
		assertTrue(multi.contains(1), "Expected mapper multi values not found");
		assertTrue(multi.contains(2), "Expected mapper multi values not found");
		assertTrue(multi.contains(3), "Expected mapper multi values not found");
		assertTrue(multi.contains(4), "Expected mapper multi values not found");
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertFalse(parentLeafNodeObject.isPresent(), "Expected absent parent object because more than 1 element");
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(4));
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_1), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_2), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_3), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(OBJECT_BRANCH_NODE), "Expected mapper multi parent object not found");
		
		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	@Test
	public void testListBranchNodeMappingSuccessWithUnionDifferent() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);

		Mapper<Object> mapper = MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode)
				.unionDifferent(MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", ObjectBranchNode::getIntLeafNode));

		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(4));
		assertTrue(paths.contains("Foo->getListBranchNodes[0]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[1]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[2]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getObjectBranchNode->getIntLeafNode"), "Expected mapper paths not found");

		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));

		Object leafNodeObject = mapper.get();
		assertNull(leafNodeObject, "Expected null leafNode object because more than 1 element");

		List<Object> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(4));
		assertTrue(multi.contains(1), "Expected mapper multi values not found");
		assertTrue(multi.contains(2), "Expected mapper multi values not found");
		assertTrue(multi.contains(3), "Expected mapper multi values not found");
		assertTrue(multi.contains(4), "Expected mapper multi values not found");

		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertFalse(parentLeafNodeObject.isPresent(), "Expected absent parent object because more than 1 element");

		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(4));
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_1), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_2), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_3), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(OBJECT_BRANCH_NODE), "Expected mapper multi parent object not found");

		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	@Test
	public void testObjectBranchNodeMappingWithUnionSame() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);
		
		Mapper<Integer> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", ObjectBranchNode::getIntLeafNode)
				.unionSame(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode));
		
		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(4));
		assertTrue(paths.contains("Foo->getListBranchNodes[0]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[1]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[2]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getObjectBranchNode->getIntLeafNode"), "Expected mapper paths not found");
		
		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));
		
		Object leafNodeObject = mapper.get();
		assertNull(leafNodeObject, "Expected null leafNode object because more than 1 element");
		
		List<Integer> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(4));
		assertTrue(multi.contains(1), "Expected mapper multi values not found");
		assertTrue(multi.contains(2), "Expected mapper multi values not found");
		assertTrue(multi.contains(3), "Expected mapper multi values not found");
		assertTrue(multi.contains(4), "Expected mapper multi values not found");
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertFalse(parentLeafNodeObject.isPresent(), "Expected absent parent object because more than 1 element");
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(4));
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_1), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_2), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_3), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(OBJECT_BRANCH_NODE), "Expected mapper multi parent object not found");
		
		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	@Test
	public void testObjectBranchNodeMappingWithUnionDifferent() {
		Foo foo = new Foo(Arrays.asList(LIST_BRANCH_NODE_1, LIST_BRANCH_NODE_2, LIST_BRANCH_NODE_3), OBJECT_BRANCH_NODE);
		
		Mapper<Object> mapper = MapperS.of(foo).map("getObjectBranchNode", Foo::getObjectBranchNode).map("getIntLeafNode", ObjectBranchNode::getIntLeafNode)
				.unionDifferent(MapperS.of(foo).mapC("getListBranchNodes", Foo::getListBranchNodes).map("getIntLeafNode", ListBranchNode::getIntLeafNode));
		
		List<String> paths = mapper.getPaths().stream().map(Mapper.Path::toString).collect(Collectors.toList());
		assertThat("Unexpected number of paths", paths.size(), is(4));
		assertTrue(paths.contains("Foo->getListBranchNodes[0]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[1]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getListBranchNodes[2]->getIntLeafNode"), "Expected mapper paths not found");
		assertTrue(paths.contains("Foo->getObjectBranchNode->getIntLeafNode"), "Expected mapper paths not found");
		
		assertThat("Unexpected number of error paths", mapper.getErrorPaths().size(), is(0));
		
		Object leafNodeObject = mapper.get();
		assertNull(leafNodeObject, "Expected null leafNode object because more than 1 element");
		
		List<Object> multi = mapper.getMulti();
		assertThat("Unexpected number of multi", multi.size(), is(4));
		assertTrue(multi.contains(1), "Expected mapper multi values not found");
		assertTrue(multi.contains(2), "Expected mapper multi values not found");
		assertTrue(multi.contains(3), "Expected mapper multi values not found");
		assertTrue(multi.contains(4), "Expected mapper multi values not found");
		
		Optional<?> parentLeafNodeObject = mapper.getParent();
		assertFalse(parentLeafNodeObject.isPresent(), "Expected absent parent object because more than 1 element");
		
		List<?> parentMulti = mapper.getParentMulti();
		assertThat("Unexpected number of multiParent", parentMulti.size(), is(4));
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_1), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_2), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(LIST_BRANCH_NODE_3), "Expected mapper multi parent object not found");
		assertTrue(parentMulti.contains(OBJECT_BRANCH_NODE), "Expected mapper multi parent object not found");
		
		assertTrue(mapper.getErrors().isEmpty(), "Expected errors to be empty");
	}
	
	private static class Foo {
		private final List<ListBranchNode> listBranchNodes;
		private final ObjectBranchNode objectBranchNode;
		
		public Foo(List<ListBranchNode> listBranchNodes, ObjectBranchNode objectBranchNode) {
			this.listBranchNodes = listBranchNodes;
			this.objectBranchNode = objectBranchNode;
		}

		public List<ListBranchNode> getListBranchNodes() {
			return listBranchNodes;
		}

		public ObjectBranchNode getObjectBranchNode() {
			return objectBranchNode;
		}
	}
	
	private static class ListBranchNode {
		private final Integer intLeafNode;
		private final List<ObjectBranchNode> objectBranchNodes;
		
		public ListBranchNode(Integer intLeafNode, List<ObjectBranchNode> objectBranchNodes) {
			this.intLeafNode = intLeafNode;
			this.objectBranchNodes = objectBranchNodes;
		}

		public Integer getIntLeafNode() {
			return intLeafNode;
		}

		public List<ObjectBranchNode> getObjectBranchNodes() {
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
