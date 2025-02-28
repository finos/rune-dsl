package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject
import com.regnosys.rosetta.types.RObjectFactory

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaExtensionsTest {
	
	@Inject extension ParseHelper<RosettaModel>
	@Inject extension RObjectFactory
	
	@Test
	def testSuperClasses() {
		val classes = '''
			namespace test
			
			type Foo extends Bar:
			type Bar extends Baz:
			type Baz:
		'''.parse.elements.filter(Data).map[buildRDataType]
		assertEquals(classes.toSet, classes.head.allSuperTypes.toSet)
		assertEquals(classes.tail.toSet, classes.get(1).allSuperTypes.toSet)
		assertEquals(#{classes.lastOrNull}, classes.get(2).allSuperTypes.toSet)
	}
	
	@Test
	def testSuperClassesWithCycle() {
		val classes = '''
			namespace test
			
			type Foo extends Bar:
			type Bar extends Baz:
			type Baz extends Foo:
		'''.parse.elements.filter(Data).map[buildRDataType]
		assertEquals(classes.toSet, classes.head.allSuperTypes.toSet)
		assertEquals(classes.toSet, classes.get(1).allSuperTypes.toSet)
		assertEquals(classes.toSet, classes.get(2).allSuperTypes.toSet)
	}
	
	@Test 
	def testEnumValue() {
		val model = '''
			namespace test
			version "1.2.3"
			
			enum Foo:
				foo0 foo1
			
			enum Bar extends Foo:
				bar
			enum Baz extends Bar:
				baz
		'''.parse
		val elems = model.elements.filter(RosettaEnumeration).map[buildREnumType]
		val foo = elems.head()
		val bar = elems.get(1)
		val baz = elems.lastOrNull()
		assertEquals(#{foo, bar, baz}, baz.allParents.toSet)
		assertEquals(#{foo, bar}, bar.allParents.toSet)
		assertEquals(#{foo}, foo.allParents.toSet)
		assertEquals(#['foo0', 'foo1', 'bar', 'baz'], baz.allEnumValues.map[name].toList)
		assertEquals(#['foo0', 'foo1', 'bar'], bar.allEnumValues.map[name].toList)
		assertEquals(#['foo0', 'foo1'], foo.allEnumValues.map[name].toList)
	}
}
