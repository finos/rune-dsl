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

@ExtendWith(InjectionExtension)
@InjectWith(MyRosettaInjectorProvider)
class AttributeValidatorTest implements RosettaIssueCodes {

	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void testAttributeNameShouldStartWithLowerCase() {
		val model =
		'''
			type PartyIdentifier:
				PartyId string (1..1)
		'''.parseRosettaWithNoErrors
		model.assertWarning(ATTRIBUTE, INVALID_CASE,
            "Attribute name should start with a lower case")
	}
	
	@Test
	def checkReferenceTypeMustHaveAKey() {
		val model = '''
			type WithKey:
			
			type TypeToUse:
				attr WithKey (0..1)
					[metadata reference]
		'''.parseRosetta
		model.assertWarning(ATTRIBUTE, null,
			"WithKey must be annotated with [metadata key] as reference annotation is used")
	}
	
	@Test
	def void testValidAttributeRestrictions() {
		'''
			type Foo:
				complexAttr Parent (1..1)
				listAttr number (0..*)
				stringAttr string (1..1)
				refAttr Ref (1..1)
				    [metadata reference]
			
			type Bar extends Foo:
				restrict complexAttr Child (1..1)
				restrict listAttr int (1..1)
				restrict stringAttr string (1..1)
					[metadata scheme]
				restrict refAttr Subref (1..1)
				    [metadata reference]
				barAttr int (1..1)
			
			// Utility types
			type Parent:
			
			type Child extends Parent:
			
			type Ref:
			  [metadata key]
			
			type Subref extends Ref:
			  [metadata key]
		'''.parseRosettaWithNoIssues
	}
	
	@Test
	def void testCannotRestrictNonExistingAttribute() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				restrict otherAttr number (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Attribute otherAttr does not exist")
	}
	
	@Test
	def void testCannotRestrictFunctionAttribute() {
		'''
			func Foo:
				inputs:
					restrict foo int (1..1)
				output:
					result int (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "You can only restrict the attribute of a type")
	}
	
	@Test
	def void testCannotRestrictAttributeToNonSubtype() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				restrict attr string (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The restricted type should be a subtype of the parent type number")
	}
	
	@Test
	def void testCannotRestrictAttributeToDifferentSubtype() {
		'''
			type Foo:
				attr Parent (0..1)
			
			type Bar extends Foo:
				restrict attr Child1 (0..1)
			
			type Qux extends Bar:
				restrict attr Child2 (0..1)
			
			type Parent:
			
			type Child1 extends Parent:
			
			type Child2 extends Parent:
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The restricted type should be a subtype of the parent type Child1")
	}
	
	@Test
	def void testCannotBroadenAttributeCardinality() {
		'''
			type Foo:
				attr string (1..1)
			
			type Bar extends Foo:
				restrict attr string (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Cardinality may not be broader than the cardinality of the parent attribute (1..1)")
	}
	
	@Test
	def void testCannotRestrictChoiceTypeToOption() {
		// Note: this should be supported once https://github.com/finos/rune-dsl/issues/797 is resolved
		'''
			choice StringOrNumber:
			    string
			    number
			
			type Foo:
			    attr StringOrNumber (1..1)
			
			type Bar extends Foo:
			    restrict attr string (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The restricted type should be a subtype of the parent type StringOrNumber")
	}
	
	@Test
	def void testCannotRestrictChoiceTypeToSubchoice() {
		// Note: this should be supported once https://github.com/finos/rune-dsl/issues/797 is resolved
		'''
			choice StringOrNumberOrBoolean:
			    string
			    number
			    boolean
			
			choice StringOrNumber:
			    string
			    number
			
			type Foo:
			    attr StringOrNumberOrBoolean (1..1)
			
			type Bar extends Foo:
			    restrict attr StringOrNumber (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The restricted type should be a subtype of the parent type StringOrNumberOrBoolean")
	}
	
	@Test
    def void supportDeprecatedAnnotationOnAttribute() {
        val model = '''
            type Foo:
            	attr int (1..1)
            		[deprecated]
            	otherAttr int (1..1)
            
            func Bar:
            	inputs:
            		foo Foo (1..1)
            	output:
            		result int (1..1)
            	
            	set result:
            		foo -> attr
        '''.parseRosetta

        model.assertWarning(ROSETTA_FEATURE_CALL, null, "attr is deprecated")
    }
	
	@Test
	def void supportDeprecatedAnnotationOnAttributeRestriction() {
		'''
			type Foo:
				attr string (1..1)
			
			type Bar extends Foo:
				restrict attr string (1..1)
					[deprecated]
			
			func Test:
				inputs:
					bar Bar (1..1)
				output:
					result string (1..1)
			
				set result:
					bar -> attr
        '''.parseRosetta
			.assertWarning(ROSETTA_FEATURE_CALL, null, "attr is deprecated")
	}
	
	@Test
	def void testCardinalityHasAtLeastOneValue() {
		'''
			type Foo:
				attr int (2..1)
		'''.parseRosetta
			.assertError(ROSETTA_CARDINALITY, null, "The upper bound must be greater than the lower bound")
	}
}