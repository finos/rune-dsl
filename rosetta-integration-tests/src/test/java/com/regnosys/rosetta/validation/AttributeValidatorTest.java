package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class AttributeValidatorTest {
	@Inject
    private RosettaValidationTestHelper validationHelper;
    @Inject
    private RosettaTestModelService modelService;
    
    @Test
	void testAttributeNameShouldStartWithLowerCase() {
		assertIssues("""
				type PartyIdentifier:
					PartyId string (1..1)
				""",
				"Attribute name should start with a lower case"
			);
	}
	
	@Test
	void checkReferenceTypeMustHaveAKey() {
		assertIssues("""
				type WithKey:
				
				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]
				""",
				"WithKey must be annotated with [metadata key] as reference annotation is used"
			);
	}
	
	@Test
	void testValidAttributeOverrides() {
		assertNoIssues("""
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
			""");
	}
	
	@Test
	void testCannotOverrideNonExistingAttribute() {
		assertIssues("""
				type Foo:
					attr number (0..1)
				
				type Bar extends Foo:
					override otherAttr number (0..1)
				""",
				"Attribute otherAttr does not exist in supertype"
			);
	}
	
	@Test
	void testCannotOverrideFunctionAttribute() {
		assertIssues("""
				func Foo:
					inputs:
						override foo int (1..1)
					output:
						result int (1..1)
				""",
				"You can only override the attribute of a type"
			);
	}
	
	@Test
	void testCannotOverrideAttributeToNonSubtype() {
		assertIssues("""
				type Foo:
					attr number (0..1)
				
				type Bar extends Foo:
					override attr string (0..1)
				""",
				"The overridden type should be a subtype of the parent type number"
			);
	}
	
	@Test
	void testCannotOverrideAttributeToDifferentSubtype() {
		assertIssues("""
				type Foo:
					attr Parent (0..1)
				
				type Bar extends Foo:
					override attr Child1 (0..1)
				
				type Qux extends Bar:
					override attr Child2 (0..1)
				
				type Parent:
				
				type Child1 extends Parent:
				
				type Child2 extends Parent:
				""",
				"The overridden type should be a subtype of the parent type Child1"
			);
	}
	
	@Test
	void testCannotBroadenAttributeCardinality() {
		assertIssues("""
				type Foo:
					attr string (1..1)
				
				type Bar extends Foo:
					override attr string (0..1)
				""",
				"Cardinality may not be broader than the cardinality of the parent attribute (1..1)"
			);
	}
	
	@Test
	void testCannotOverrideChoiceTypeToOption() {
		// Note: this should be supported once https://github.com/finos/rune-dsl/issues/797 is resolved
		assertIssues("""
				choice StringOrNumber:
				    string
				    number
				
				type Foo:
				    attr StringOrNumber (1..1)
				
				type Bar extends Foo:
				    override attr string (1..1)
				""",
				"The overridden type should be a subtype of the parent type StringOrNumber"
			);
	}
	
	@Test
	void testCannotOverrideChoiceTypeToSubchoice() {
		// Note: this should be supported once https://github.com/finos/rune-dsl/issues/797 is resolved
		assertIssues("""
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
				""",
				"The overridden type should be a subtype of the parent type StringOrNumberOrBoolean"
			);
	}
	
	@Test
	void testMustOverrideRuleReferenceWhenRestrictingType() {
		assertIssues("""
				type Parent:
				type Child extends Parent:
				
				type Foo:
				    attr Parent (1..1)
				    	[ruleReference AttrRule]
				
				type Bar extends Foo:
				    override attr Child (1..1)
				
				reporting rule AttrRule from Parent:
					item
				""",
				"The overridden type is incompatible with the inherited rule reference `AttrRule`. Either change the type or override the rule reference"
			);
	}
	
	@Test
	void testMustNotOverrideRuleReferenceWhenRestrictingTypeToCompatibleType() {
		assertNoIssues("""
				type Parent:
				type Child extends Parent:
				
				type Foo:
				    attr Parent (1..1)
				    	[ruleReference AttrRule]
				
				type Bar extends Foo:
				    override attr Child (1..1)
				
				reporting rule AttrRule from Child:
					item
				"""
			);
	}
	
	@Test
    void supportDeprecatedAnnotationOnAttribute() {
		assertIssues("""
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
				""",
				"attr is deprecated"
			);
    }
	
	@Test
	void inheritDeprecatedAnnotationOnAttributeOverride() {
		assertIssues("""
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
				""",
				"attr is deprecated"
			);
	}
	
	@Test
	void testCardinalityHasAtLeastOneValue() {
		assertIssues("""
				type Foo:
					attr int (2..1)
				""",
				"The upper bound must be greater than the lower bound"
			);
	}
	
	private void assertIssues(String model, String expectedIssues) {
		RosettaTestModel parsedModel = modelService.toTestModel(model, false);
		validationHelper.assertIssues(parsedModel.getModel(), expectedIssues);
	}
	
	private void assertNoIssues(String model) {
		modelService.toTestModel(model, true);
	}
}
