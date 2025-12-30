package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ReportValidatorTest extends AbstractValidatorTest {
	@Test
	void testRuleReferenceToRuleWithLabelIsDeprecated() {
		assertIssues("""
				type Foo:
					bar Bar (1..1)
						[ruleReference for barAttr RuleWithLabel]
				
				type Bar:
					barAttr string (1..1)
				
				reporting rule RuleWithLabel from number:
					empty
					as "My label"
				""",
				"""
				WARNING (null) 'Specifying a label in a reporting rule is deprecated. Add a `label` annotation instead' at 6:30, length 13, on RuleReferenceAnnotation
				""");
	}
	
	@Test
	void testRuleReferenceCardinalityMismatch() {
		assertIssues("""
				type Foo:
					bar Bar (1..1)
						[ruleReference for barAttr RuleReturnsMulti]
				
				type Bar:
					barAttr string (1..1)
				
				reporting rule RuleReturnsMulti from number:
					[ "1", "2" ]
				""",
				"""
				WARNING (null) 'Expected single cardinality for barAttr, but rule has multi cardinality' at 6:3, length 44, on RuleReferenceAnnotation
				""");
	}
	
	@Test
	void testRuleReferenceCardinalityMismatch2() {
		assertIssues("""
				type Foo:
					bar Bar (1..1)
						[ruleReference for barAttr RuleReturnsMulti]
				
				type Bar:
					barAttr string (1..1)
				
				reporting rule RuleReturnsMulti from number:
					FuncReturnsMulti
				
				func FuncReturnsMulti:
					inputs:
						inNum number (0..1)
					output:
						out	string (0..*)
					
					set out:
						[ "1", "2" ]
				""",
				"""
				WARNING (null) 'Expected single cardinality for barAttr, but rule has multi cardinality' at 6:3, length 44, on RuleReferenceAnnotation
				""");
	}
	
	@Test
	void testInvalidInputType() {
		assertIssues("""
				type Foo:
					fooAttr string (1..1)
						[ruleReference StringInput]
					bar Bar (1..1)
						[ruleReference for barAttr NumberInput] // error should be on this annotation
				
				type Bar:
					barAttr string (1..1)
				
				reporting rule NumberInput from number:
					empty
				
				reporting rule StringInput from string:
					empty
				""",
				"""
				ERROR (null) 'Rule `NumberInput` expects an input of type `number`, while previous rules expect an input of type `string`' at 8:30, length 11, on RuleReferenceAnnotation
				""");
	}
	
	@Test
	void testInvalidNestedInputType() {
		assertIssues("""
				type Foo:
					bar Bar (1..1)
				
				type Bar:
					barAttr1 string (1..1)
						[ruleReference StringInput]
					barAttr2 string (1..1)
						[ruleReference StringInput]
				
				type FooExtended extends Foo:
					override bar Bar (1..1)
						[ruleReference for barAttr1 NumberInput] // this should cause an error on the type.
																 //	Note: if this one was on barAttr2 instead, it could show an error on the rule reference instead.
				
				reporting rule NumberInput from number:
					empty
				
				reporting rule StringInput from string:
					empty
				""",
				"""
				ERROR (null) 'Rule `StringInput` for bar -> barAttr2 expects an input of type `string`, while previous rules expect an input of type `number`' at 14:15, length 3, on Attribute
				""");
	}
	
	@Test
	void testValidNestedInputTypeByRemovingRule() {
		assertNoIssues("""
				type Foo:
					fooAttr string (1..1)
					bar Bar (1..1)
						[ruleReference NumberInput]
				
				type Bar:
					barAttr string (1..1)
						[ruleReference StringInput]
				
				type FooExtended extends Foo:
					override fooAttr string (1..1)
						[ruleReference StringInput]
					override bar Bar (1..1)
						[ruleReference empty] // without this line, there should be an error on the type `Bar` of the `override bar` attribute
				
				reporting rule NumberInput from number:
					empty
				
				reporting rule StringInput from string:
					empty
				""");
	}
}
