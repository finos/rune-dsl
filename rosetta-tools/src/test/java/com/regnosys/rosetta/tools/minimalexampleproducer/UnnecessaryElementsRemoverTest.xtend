package com.regnosys.rosetta.tools.minimalexampleproducer

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.^extension.ExtendWith
import javax.inject.Inject
import com.regnosys.rosetta.tools.minimalexampleproducer.UnnecessaryElementsRemover
import org.junit.jupiter.api.Test

import static org.junit.Assert.*
import org.eclipse.xtext.testing.util.ParseHelper
import com.regnosys.rosetta.rosetta.RosettaModel
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import com.regnosys.rosetta.builtin.RosettaBuiltinsService
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.serializer.impl.Serializer
import com.regnosys.rosetta.rosetta.RosettaRule

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class UnnecessaryElementsRemoverTest {
	@Inject UnnecessaryElementsRemover service
	@Inject Serializer serializer
	@Inject extension ParseHelper<RosettaModel>
	@Inject extension ValidationTestHelper
	@Inject RosettaBuiltinsService builtins
	
	@Inject
	XtextResourceSet resourceSet
	
	@Test
	def void testElementsRemover() {
		resourceSet.getResource(builtins.basicTypesURI, true)
		resourceSet.getResource(builtins.annotationsURI, true)
		val model1 = parse('''
			namespace a
			
			enum MyEnum:
				VALUE1
				VALUE2
			
			type Foo:
				attr1 int (0..1)
				attr2 int (0..1)
				attr3 string (0..1)
			''', resourceSet)
		val model2 = parse('''
			namespace b
			
			import a.*
			import c.*
			
			func F: <"Definition of F">
				inputs:
					a string (1..1)
				output:
					result MyEnum (1..1)
				
				set result:
					if Unnecessary {} = Unnecessary {}
					then MyEnum -> VALUE1
					else MyEnum -> VALUE2
			
			reporting rule R from string:
				if F = MyEnum -> VALUE1
				then Foo { attr1: 0, ... }
				else Foo { attr2: 42, ... }
			''', resourceSet)
		val model3 = parse('''
			namespace c
			
			type Unnecessary:
			''', resourceSet)
				
		val rule = model2.elements
			.findFirst[it instanceof RosettaRule && (it as RosettaRule).name == "R"]
		
		resourceSet.resources.forEach[assertNoIssues]
		assertEquals(5, resourceSet.resources.size)
		assertTrue(resourceSet.resources.contains(model3.eResource))
		
		service.removeUnnecessaryElementsFromResourceSet(rule, false)
		
		resourceSet.resources.forEach[assertNoIssues]
		assertEquals(4, resourceSet.resources.size)
		assertFalse(resourceSet.resources.contains(model3.eResource))
		
		val expectedModel1 = '''
		namespace a
		
		enum MyEnum:
			VALUE1
		
		type Foo:
			attr1 int (0..1)
			attr2 int (0..1)
		'''
		assertEquals(expectedModel1, serializer.serialize(model1))
		
		val expectedModel2 = '''
		namespace b
		
		import a.*
		
		func F:
			inputs:
				a string (1..1)
			output:
				result MyEnum (1..1)
		
		reporting rule R from string:
			if F = MyEnum -> VALUE1
			then Foo { attr1: 0, ... }
			else Foo { attr2: 42, ... }
		'''
		assertEquals(expectedModel2, serializer.serialize(model2))
	}
}
