package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import java.util.HashMap
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class NamespaceHierarchyGeneratorTest {
	
	@Inject extension NamespaceHierarchyGenerator namespaceHierarchyGenerator  
	
	@Test
	def void testNamespaceHierarchy() {
		var namespaceDescriptionMap = new HashMap()
		namespaceDescriptionMap.put("cdm.base", #["this is base description", "second description"])
		namespaceDescriptionMap.put("cdm.base.staticdata.datetime", #["this is datetime description"])
		
		var namespaceUriMap = new HashMap()
		namespaceUriMap.put("cdm.base.staticdata.datetime", #["base-staticdata-datetime-enum.rosetta", "base-staticdata-datetime-type.rosetta"])
		namespaceUriMap.put("org.isda.cdm", #["model-cdm-event.rosetta", "model-cdm-function.rosetta"])
		
		
		var fsa = mock(IFileSystemAccess2)
		val root = namespaceHierarchyGenerator.generateNamespacePackageHierarchy(fsa, namespaceDescriptionMap, namespaceUriMap)
		assertNotNull(root, "not null")
		assertThat(root, containsString("this is datetime description"))
	}
	
}