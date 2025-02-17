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
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
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
	def void testValidAttributeOverrides() {
		'''
			type Foo:
				complexAttr Parent (1..1)
				listAttr number (0..*)
				stringAttr string (1..1)
					[metadata scheme]
				refAttr Ref (1..1)
				    [metadata reference]
			
			type Bar extends Foo:
				override complexAttr Child (1..1)
				override listAttr int (1..1)
					[metadata scheme]
				override stringAttr string(maxLength: 42) (1..1)
				override refAttr Subref (1..1)
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
	def void testCannotOverrideNonExistingAttribute() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				override otherAttr number (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Attribute otherAttr does not exist in supertype")
	}
	
	@Test
	def void testCannotOverrideFunctionAttribute() {
		'''
			func Foo:
				inputs:
					override foo int (1..1)
				output:
					result int (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "You can only override the attribute of a type")
	}
	
	@Test
	def void testCannotOverrideAttributeToNonSubtype() {
		'''
			type Foo:
				attr number (0..1)
			
			type Bar extends Foo:
				override attr string (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The overridden type should be a subtype of the parent type number")
	}
	
	@Test
	def void testCannotOverrideAttributeToDifferentSubtype() {
		'''
			type Foo:
				attr Parent (0..1)
			
			type Bar extends Foo:
				override attr Child1 (0..1)
			
			type Qux extends Bar:
				override attr Child2 (0..1)
			
			type Parent:
			
			type Child1 extends Parent:
			
			type Child2 extends Parent:
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The overridden type should be a subtype of the parent type Child1")
	}
	
	@Test
	def void testCannotBroadenAttributeCardinality() {
		'''
			type Foo:
				attr string (1..1)
			
			type Bar extends Foo:
				override attr string (0..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "Cardinality may not be broader than the cardinality of the parent attribute (1..1)")
	}
	
	@Test
	def void testCannotOverrideChoiceTypeToOption() {
		// Note: this should be supported once https://github.com/finos/rune-dsl/issues/797 is resolved
		'''
			choice StringOrNumber:
			    string
			    number
			
			type Foo:
			    attr StringOrNumber (1..1)
			
			type Bar extends Foo:
			    override attr string (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The overridden type should be a subtype of the parent type StringOrNumber")
	}
	
	@Test
	def void testCannotOverrideChoiceTypeToSubchoice() {
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
			    override attr StringOrNumber (1..1)
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The overridden type should be a subtype of the parent type StringOrNumberOrBoolean")
	}
	
	@Test
	def void testMustOverrideRuleReferenceWhenRestrictingType() {
		'''
			type Parent:
			type Child extends Parent:
			
			type Foo:
			    attr Parent (1..1)
			    	[ruleReference AttrRule]
			
			type Bar extends Foo:
			    override attr Child (1..1)
			
			reporting rule AttrRule from Parent:
				item
		'''.parseRosetta
			.assertError(ATTRIBUTE, null, "The overridden type is incompatible with the inherited rule reference `AttrRule`. Either change the type or override the rule reference")
	}
	
	@Test
	def void testMustNotOverrideRuleReferenceWhenRestrictingTypeToCompatibleType() {
		'''
			type Parent:
			type Child extends Parent:
			
			type Foo:
			    attr Parent (1..1)
			    	[ruleReference AttrRule]
			
			type Bar extends Foo:
			    override attr Child (1..1)
			
			reporting rule AttrRule from Child:
				item
		'''.parseRosettaWithNoIssues
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
	def void inheritDeprecatedAnnotationOnAttributeOverride() {
		'''
			type Foo:
				attr string (1..1)
					[deprecated]
			
			type Bar extends Foo:
				override attr string (1..1)
			
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