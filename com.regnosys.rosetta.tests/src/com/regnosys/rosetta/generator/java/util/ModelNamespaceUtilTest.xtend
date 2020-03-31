package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ModelNamespaceUtilTest implements RosettaIssueCodes {
	
	@Inject extension ModelHelper modelHelper
	@Inject extension ModelNamespaceUtil modelNamespaceUtil
	
	
	@Test
	def void testNamespaceDescriptionMap() {
		
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
				
		val namespaceMap = modelNamespaceUtil.generateNamespaceDescriptionMap(newArrayList(model1, model2, model3), "0.0.1").asMap		
		assertEquals(2, namespaceMap.size)
		
		val descriptionList1 = namespaceMap.get("cdm.test")
		assertEquals(2, descriptionList1.size)
		
		
		val descriptionList2 = namespaceMap.get("cdm.another.namesapce")
		assertEquals(1, descriptionList2.size)
		assertEquals("another description", descriptionList2.get(0))
	}
}