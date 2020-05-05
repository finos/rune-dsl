package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ModelNamespaceUtilTest {
	
	@Inject extension ModelHelper modelHelper
	@Inject extension ModelNamespaceUtil modelNamespaceUtil
	
	@Test
	def void testMultipleDescriptionsForSameNamespace() {
		
		val resource1 = 
		'''
			namespace cdm.test : <"description for enum">
			version "test"
			
			enum Enum: A B
			type Foo:
				attr Enum (0..1)			
		'''
		
		val resource2 =
		'''
			namespace cdm.test : <"description for type">
			version "test"
			
			type Foobar:
				attr string (0..1)
			
		'''
		
		val resource3 =
		'''
			namespace cdm.another.namesapce : <"another description">
			version "test"
			
			type Bazzinga:
				attr string (0..1)
			
		'''
		
		val model1 = modelHelper.parseRosetta(resource1)
		val model2 = modelHelper.parseRosetta(resource2)
		val model3 = modelHelper.parseRosetta(resource3)
				
		val namespaceMap = modelNamespaceUtil.namespaceToDescriptionMap(newArrayList(model1, model2, model3)).asMap		
		assertEquals(2, namespaceMap.size)
		
		val namespace1Map = namespaceMap.get("cdm.test")
		assertEquals(2, namespace1Map.size)
		
		
		val namespace2Map = namespaceMap.get("cdm.another.namesapce")
		assertEquals(1, namespace2Map.size)
		assertEquals("another description", namespace2Map.get(0))
	}
	
	@Test
	def void testMixOfDescriptionAndNonDescriptionNamespace() {
		
		val resource1 = 
		'''
			namespace cdm.test : <"description for enum">
			version "test"
			
			enum Enum: A B
			type Foo:
				attr Enum (0..1)			
		'''
		
		val resource2 =
		'''
			namespace cdm.another.namesapce
			version "test"
			
			type Bazzinga:
				attr string (0..1)
			
		'''
		
		val model1 = modelHelper.parseRosetta(resource1)
		val model2 = modelHelper.parseRosetta(resource2)
				
		val namespaceMap = modelNamespaceUtil.namespaceToDescriptionMap(newArrayList(model1, model2)).asMap		
		assertEquals(1, namespaceMap.size)
		
		val namespace1Map = namespaceMap.get("cdm.test")
		assertEquals(1, namespace1Map.size)
		assertEquals("description for enum", namespace1Map.get(0))
	}
	
	@Test
	def void testNoDescription() {
		
		val resource1 =
		'''
			namespace cdm.another.namesapce
			version "test"
			
			type Bazzinga:
				attr string (0..1)
			
		'''
		
		val model = modelHelper.parseRosetta(resource1)
					
		val namespaceMap = modelNamespaceUtil.namespaceToDescriptionMap(newArrayList(model)).asMap		
		assertEquals(0, namespaceMap.size)
	}
	
	@Test
	def void testNamespaceToModelUriMap() {
		val resource1 =
		'''
			namespace cdm.another.namesapce
			version "test"
			
			type Bazzinga:
				attr string (0..1)
			
		'''
		
		val model = modelHelper.parseRosetta(resource1)
				
		val namespaceMap = modelNamespaceUtil.namespaceToModelUriMap(newArrayList(model)).asMap		
		assertEquals(1, namespaceMap.size)
	}

}