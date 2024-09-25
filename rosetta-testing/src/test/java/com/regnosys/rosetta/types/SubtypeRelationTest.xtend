package com.regnosys.rosetta.types

import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.^extension.ExtendWith
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.ModelHelper

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.tests.util.ExpressionParser
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RStringType


@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class SubtypeRelationTest {
	@Inject extension SubtypeRelation
	@Inject extension ModelHelper
	@Inject extension RObjectFactory
	@Inject extension ExpressionParser
	@Inject extension RosettaTypeProvider
	@Inject extension RBuiltinTypeService
	
	
	private def Data getData(RosettaModel model, String name) {
		return model.elements.filter(RosettaNamed).findFirst[it.name == name] as Data
	}
	private def RosettaEnumeration getEnum(RosettaModel model, String name) {
		return model.elements.filter(RosettaNamed).findFirst[it.name == name] as RosettaEnumeration
	}
	
	@Test
	def testJoinOnSameBaseTypeWithMetaIsCorrect() {
		val fieldA = '''
			fieldA string (1..1)
				[metadata scheme]
				[metadata reference]
		'''.parseAttribute.RTypeOfSymbol
		
		val fieldB = '''
			fieldB string (1..1)
				[metadata scheme]
				[metadata address]
		'''.parseAttribute.RTypeOfSymbol
				
		val result = fieldA.join(fieldB)
		assertTrue(result.RType instanceof RStringType)
		assertEquals(#[new RMetaAttribute("scheme", UNCONSTRAINED_STRING)], result.getMetaAttributes)
	}
	
	@Test
	def testStringWithMetaIsSubtypeOfStringWithMeta() {
		val fieldA = '''
			fieldA string (1..1)
				[metadata scheme]
		'''.parseAttribute.RTypeOfSymbol
		
		val fieldB = '''
			fieldB string (1..1)
				[metadata scheme]
		'''.parseAttribute.RTypeOfSymbol
		
		assertTrue(fieldA.isSubtypeOf(fieldB))
		assertTrue(fieldB.isSubtypeOf(fieldA))
	}
	
	@Test
	def testStringWithSchemeIsNotSubtypeOfStringWithSchemeAndReference() {
		val fieldA = '''
			fieldA string (1..1)
				[metadata scheme]
				[metadata reference]
		'''.parseAttribute.RTypeOfSymbol
		
		val fieldB = '''
			fieldB string (1..1)
				[metadata scheme]
		'''.parseAttribute.RTypeOfSymbol
		
		assertFalse(fieldB.isSubtypeOf(fieldA))
	}
	
	@Test
	def testStringWithSchemeAndReferenceIsSubTypeOfStringWithScheme() {
		val fieldA = '''
			fieldA string (1..1)
				[metadata scheme]
				[metadata reference]
		'''.parseAttribute.RTypeOfSymbol
		
		val fieldB = '''
			fieldB string (1..1)
				[metadata scheme]
		'''.parseAttribute.RTypeOfSymbol
		
		assertTrue(fieldA.isSubtypeOf(fieldB))
	}
	
	@Test
	def testExtendedTypeIsSubtype() {
		val model = '''
		type A:
		type B extends A:
		'''.parseRosettaWithNoIssues
		
		val a = model.getData('A').buildRDataType
		val b = model.getData('B').buildRDataType
		
		assertTrue(b.isSubtypeOf(a))
	}
	
	@Test
	def testJoinTypeHierarchy() {
		val model = '''
		type A:
		type B extends A:
		type C extends A:
		type D extends C:
		'''.parseRosettaWithNoIssues
		
		val a = model.getData('A').buildRDataType
		val b = model.getData('B').buildRDataType
		val d = model.getData('D').buildRDataType
		
		assertEquals(a, join(b, d))
	}
	
	@Test
	def testExtendedEnumIsNotASupertype() {
		val model = '''
		enum A:
		enum B extends A:
		'''.parseRosettaWithNoIssues
		
		val a = model.getEnum('A').buildREnumType
		val b = model.getEnum('B').buildREnumType
		
		assertFalse(a.isSubtypeOf(b))
	}
}
