package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class AttributeValidatorTest extends AbstractValidatorTest {
    
    @Test
    void testDeepPathInRuleReferenceIsDisallowed() {
    	assertIssues("""
				type Foo:
					opt C (1..1)
						[ruleReference for item ->> id IdRule]
				
				choice C:
					Opt1
					Opt2
				
				type Opt1:
					id string (1..1)
				
				type Opt2:
					id string (1..1)
    			
    			reporting rule IdRule from int:
    				"Test"
				""",
				"""
				ERROR (null) 'Deep paths are not allowed for `ruleReference` annotations' at 6:27, length 3, on AnnotationDeepPath
				"""
			);
    }
    
    @Test
    void testPathInRuleReferenceOnMultiCardinalityAttributeIsDisallowed() {
    	assertIssues("""
				type Foo:
					bars Bar (0..*)
						[ruleReference for attr AttrRule]
				
				type Bar:
					attr string (1..1)
				
				reporting rule AttrRule from string:
					item
				""",
				"""
				ERROR (null) 'Paths on multi-cardinality attributes are not allowed' at 6:22, length 4, on RuleReferenceAnnotation
				"""
			);
    }
    
    @Test
    void testPathInRuleReferenceContainingMultiCardinalityAttributeIsDisallowed() {
    	assertIssues("""
				type Foo:
					bars Bar (1..1)
						[ruleReference for quxs -> attr AttrRule]
				
				type Bar:
					quxs Qux (0..*)
				
				type Qux:
					attr string (1..1)
				
				reporting rule AttrRule from string:
					item
				""",
				"""
				ERROR (null) 'Paths on multi-cardinality attributes are not allowed' at 6:27, length 2, on AnnotationPath
				"""
			);
    }
    
    @Test
    void testPathInRuleReferenceEndingWithMultiCardinalityAttributeIsAllowed() {
    	assertNoIssues("""
				type Foo:
					bars Bar (1..1)
						[ruleReference for attr AttrRule]
				
				type Bar:
					attr string (0..*)
				
				reporting rule AttrRule from string:
					item
				"""
			);
    }
    
    @Test
    void testCannotEmptyNonExistingRuleReference() {
    	assertIssues("""
				type Foo:
					attr string (1..1)
				
				type Bar extends Foo:
					override attr string (1..1)
						[ruleReference empty]
				""",
				"""
				ERROR (null) 'There is no rule reference to remove' at 9:18, length 5, on RuleReferenceAnnotation
				"""
			);
    }
    
    @Test
    void testInvalidCircularReport() {
    	assertNoIssues("""
				type Foo:
					anotherFoo Foo (0..1)
					attr string (1..1)
						[ruleReference Attr]
				
				reporting rule Attr from int:
					"Test"
				"""
			);
    }
    
    @Test
    void testValidCircular() {
    	assertNoIssues("""
    			type Foo:
					bar Bar (1..1)
						[ruleReference for attr AttrRule]
				
				type Bar:
					attr string (1..1)
					bar Bar (0..1)
				
				reporting rule AttrRule from string:
					item
    			""");
    }
    
    @Test
	void testAttributeNameShouldStartWithLowerCase() {
		assertIssues("""
				type PartyIdentifier:
					PartyId string (1..1)
				""",
				"""
				WARNING (RosettaIssueCodes.invalidCase) 'Attribute name should start with a lower case' at 5:2, length 7, on Attribute
				"""
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
				"""
				WARNING (null) 'WithKey must be annotated with [metadata key] as reference annotation is used' at 7:7, length 7, on Attribute
				"""
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
				"""
				ERROR (null) 'Attribute otherAttr does not exist in supertype' at 8:11, length 9, on Attribute
				"""
			);
	}
	
	@Test
	void testCannotOverrideFunctionAttribute() {
		assertIssues("""
				func Foo:
					[codeImplementation]
					inputs:
						override foo int (1..1)
					output:
						result int (1..1)
				""",
				"""
				ERROR (null) 'You can only override the attribute of a type' at 7:3, length 8, on Attribute
				"""
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
				"""
				ERROR (null) 'The overridden type should be a subtype of the parent type number' at 8:16, length 6, on Attribute
				"""
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
				"""
				ERROR (null) 'The overridden type should be a subtype of the parent type Child1' at 11:16, length 6, on Attribute
				"""
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
				"""
				ERROR (null) 'Cardinality may not be broader than the cardinality of the parent attribute (1..1)' at 8:23, length 6, on Attribute
				"""
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
				"ERROR (null) 'The overridden type should be a subtype of the parent type StringOrNumber' at 12:19, length 6, on Attribute"
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
				"""
				ERROR (null) 'The overridden type should be a subtype of the parent type StringOrNumberOrBoolean' at 17:19, length 14, on Attribute
				"""
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
				"""
				ERROR (null) 'The overridden type is incompatible with the inherited rule reference `AttrRule`. Either change the type or override the rule reference' at 12:19, length 5, on Attribute
				"""
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
	void testMustOverrideNestedRuleReferenceWhenRestrictingType() {
		assertIssues("""
				type Parent:
					qux SuperQux (1..1)
				type Child extends Parent:
					override qux Qux (1..1)
				
				type SuperQux:
				type Qux extends SuperQux:
				
				type Foo:
				    attr Parent (1..1)
				    	[ruleReference for qux SuperQuxRule]
				
				type Bar extends Foo:
				    override attr Child (1..1)
				
				reporting rule SuperQuxRule from SuperQux:
					item
				""",
				"""
				ERROR (null) 'The overridden type is incompatible with the inherited rule reference `SuperQuxRule` for qux. Either change the type or override the rule reference' at 17:19, length 5, on Attribute
				"""
			);
	}
	
	@Test
	void testMustNotOverrideNestedRuleReferenceWhenRestrictingTypeToCompatibleType() {
		assertNoIssues("""
				type Parent:
					n number (1..1)
				type Child extends Parent:
					override n int (1..1)
				
				type Foo:
				    attr Parent (1..1)
				    	[ruleReference for n NRule]
				
				type Bar extends Foo:
				    override attr Child (1..1)
				
				reporting rule NRule from int:
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
				"""
				WARNING (null) 'attr is deprecated' at 16:19, length 4, on RosettaFeatureCall
				"""
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
				"""
				WARNING (null) 'attr is deprecated' at 9:11, length 4, on Attribute
				WARNING (null) 'attr is deprecated' at 18:10, length 4, on RosettaFeatureCall
				"""
			);
	}
	
	@Test
	void testCardinalityHasAtLeastOneValue() {
		assertIssues("""
				type Foo:
					attr int (2..1)
				""",
				"""
				ERROR (null) 'The upper bound must be greater than the lower bound' at 5:11, length 6, on RosettaCardinality
				"""
			);
	}
}
