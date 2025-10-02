package com.regnosys.rosetta.resource

import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaFragmentProviderTest {
	
	@Inject extension ParseHelper<RosettaModel> 
	
	@Test
	def testURIFragments() {
		val clazz = '''
			namespace test
			version "1.2.3"
			
			type Foo:
				foo Foo (1..1)
		'''.parse.elements.filter(Data).head
		val resourceSet = clazz.eResource.resourceSet
		val classURI = EcoreUtil.getURI(clazz)
		assertEquals('test.Foo', classURI.fragment)
		assertEquals(clazz, resourceSet.getEObject(classURI, false))
		
		val attribute = clazz.attributes.head
		val attributeURI = EcoreUtil.getURI(attribute)
		assertEquals('test.Foo.foo', attributeURI.fragment)
		assertEquals(attribute, resourceSet.getEObject(attributeURI, false))
	}
}
