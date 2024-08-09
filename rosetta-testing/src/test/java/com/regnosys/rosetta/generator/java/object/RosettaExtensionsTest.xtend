package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject
import com.regnosys.rosetta.types.TypeSystem

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaExtensionsTest {
	
	@Inject extension ParseHelper<RosettaModel> 
	@Inject extension RosettaExtensions
	@Inject extension TypeSystem
	
	@Test
	def testSuperClasses() {
		val classes = '''
			namespace test
			
			type Foo extends Bar:
			type Bar extends Baz:
			type Baz:
		'''.parse.elements.filter(Data).map[dataToType]
		assertEquals(classes.toSet, classes.head.allSuperTypes)
		assertEquals(classes.tail.toSet, classes.get(1).allSuperTypes)
		assertEquals(#{classes.last}, classes.get(2).allSuperTypes)
	}
	
	@Test
	def testSuperClassesWithCycle() {
		val classes = '''
			namespace test
			
			type Foo extends Bar:
			type Bar extends Baz:
			type Baz extends Foo:
		'''.parse.elements.filter(Data).map[dataToType]
		assertEquals(classes.toSet, classes.head.allSuperTypes)
		assertEquals(classes.toSet, classes.get(1).allSuperTypes)
		assertEquals(classes.toSet, classes.get(2).allSuperTypes)
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
		val foo = model.elements.filter(RosettaEnumeration).head()
		val bar = model.elements.filter(RosettaEnumeration).get(1)
		val baz = model.elements.filter(RosettaEnumeration).last()
		assertEquals(#{foo, bar, baz}, baz.allSuperEnumerations)
		assertEquals(#{foo, bar}, bar.allSuperEnumerations)
		assertEquals(#{foo}, foo.allSuperEnumerations)
		assertEquals(#['baz', 'bar', 'foo0', 'foo1'], baz.allEnumValues.map[name].toList)
		assertEquals(#['bar', 'foo0', 'foo1'], bar.allEnumValues.map[name].toList)
		assertEquals(#['foo0', 'foo1'], foo.allEnumValues.map[name].toList)
	}
}
