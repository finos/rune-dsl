package com.regnosys.rosetta.types

import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.^extension.ExtendWith
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
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
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
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
	def testJoinOnMetaSubTypesReturnsParent() {
		val model = '''
			type A:
			
			type B extends A:
			
			type C extends A:
				
		'''.parseRosettaWithNoIssues
		
		val fieldBType = '''
			fieldB B (1..1)
				[metadata reference]
		'''.parseAttribute(#[model]).RTypeOfSymbol
		
		val fieldCType = '''
			fieldC C (1..1)
				[metadata reference]
		'''.parseAttribute(#[model]).RTypeOfSymbol
		
		
		val fieldA = model.getData("A").buildRDataType
		
		val joined = fieldBType.join(fieldCType)
		
		assertEquals(fieldA.withNoMeta, joined)
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
		val resultMetaAttribute = result.getMetaAttributes.get(0)
		assertEquals("scheme", resultMetaAttribute.name)
		assertEquals(UNCONSTRAINED_STRING, resultMetaAttribute.RType)
	}
	
	@Test
	def testStringWithSchemeAndReferenceIsSubtypeOfRelationWithStringWithAddressAndLocation() {
		val fieldAType = '''
			fieldA string (1..1)
				[metadata scheme]
				[metadata reference]
		'''.parseAttribute.RTypeOfSymbol
		
		val fieldBType = '''
			fieldB string (1..1)
				[metadata address]
				[metadata location]
		'''.parseAttribute.RTypeOfSymbol
		
		assertTrue(fieldAType.isSubtypeOf(fieldBType, true))
		assertTrue(fieldBType.isSubtypeOf(fieldAType, true))
	}
	
	@Test
	def testStringWithSchemeIsSubtypeOfStringWithScheme() {
		val fieldAType = '''
			fieldA string (1..1)
				[metadata scheme]
		'''.parseAttribute.RTypeOfSymbol
		
		assertTrue(fieldAType.isSubtypeOf(fieldAType, true))
	}
	
	@Test
	def testExtendedTypeIsSubtype() {
		val model = '''
		type A:
		type B extends A:
		'''.parseRosettaWithNoIssues
		
		val a = model.getData('A').buildRDataType
		val b = model.getData('B').buildRDataType
		
		assertTrue(b.isSubtypeOf(a, true))
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
		
		assertFalse(a.isSubtypeOf(b, true))
	}
}
