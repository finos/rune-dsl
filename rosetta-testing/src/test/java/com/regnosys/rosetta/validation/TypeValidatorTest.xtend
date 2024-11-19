package com.regnosys.rosetta.validation

import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import javax.inject.Inject
import org.eclipse.xtext.diagnostics.Diagnostic

@ExtendWith(InjectionExtension)
@InjectWith(MyRosettaInjectorProvider)
class TypeValidatorTest implements RosettaIssueCodes {

	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
		
	@Test
	def void testTypeNameShouldBeCapitalized() {
		'''
			type partyIdentifier:
				partyId string (1..1)
		'''.parseRosettaWithNoErrors
			.assertWarning(DATA, INVALID_CASE, "Type name should start with a capital")
	}
	
	@Test
	def void testTypeNameMayStartWithUnderscore() {
		'''
			type _PartyIdentifier:
				partyId string (1..1)
		'''.parseRosettaWithNoIssues
	}
	
	@Test
	def void testAttributeRestrictionsMustComeFirst() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				barAttr int (1..1)
				restrict attr number (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Attribute restrictions should come before any new attributes.")
	}
	
	@Test
	def void testCannotRestrictAttributeTwice() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				restrict attr number (1..1)
				restrict attr int (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Duplicate attribute restriction for 'attr'.")
	}
	
	@Test
	def void testOverridingAttributeWithoutKeywordIsDeprecated() {
		// Note: once support is dropped, this should become a duplicate attribute error.
		'''
			type Foo:
				attr string (1..1)
			
			type Bar extends Foo:
				attr string (1..1)
        '''.parseRosetta
			.assertWarning(ATTRIBUTE, null, "Duplicate attribute 'attr'. To override the ruleReference or synonym annotations on this attribute, use a rule source or synonym source instead. To restrict the type or cardinality of this attribute, use the keyword `restrict`.")
	}
	
	@Test
	def void testSuperTypeMayNotBeAChoiceType() {
		val model = '''
		choice StringOrNumber:
			string
			number
		
		type Foo extends StringOrNumber:
		'''.parseRosetta
		
		model.assertError(DATA, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Data 'StringOrNumber'")
	}
	
	@Test
	def void testSuperTypesCannotHaveACycle() {
		val model = '''
		type A extends C:
		
		type B extends A:
		
		type C extends B:
		'''.parseRosetta
		
		model.assertError(DATA, null, "Cyclic extension: A extends C extends B extends A")
		model.assertError(DATA, null, "Cyclic extension: B extends A extends C extends B")
		model.assertError(DATA, null, "Cyclic extension: C extends B extends A extends C")
	}
	
	@Test
	def void supportDeprecatedAnnotationOnType() {
        val model = '''
            type TestTypeDeprecated:
            	[deprecated]
            	attr int (1..1)
            
            func Foo:
            	output:
            		result TestTypeDeprecated (1..1)
            	
            	set result:
            		TestTypeDeprecated { attr: 42 }
        '''.parseRosetta

        model.assertWarning(TYPE_CALL, null, "TestTypeDeprecated is deprecated")
    }
}