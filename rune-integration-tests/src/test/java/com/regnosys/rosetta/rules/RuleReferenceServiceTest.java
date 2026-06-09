package com.regnosys.rosetta.rules;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RuleReferenceServiceTest {
	@Inject
	private RuleReferenceService ruleService;
	@Inject
	private RosettaTestModelService modelService;
	@Inject
	private RObjectFactory objectFactory;
	
	@Test
	void testDoNotTraverseMultiCardinalityAttributes() {
		assertTraversal(
				"Foo",
				"""
				type Foo:
					bars Bar (0..*)
				
				type Bar:
					attr string (1..1)
						[ruleReference AttrRule]
				
				reporting rule AttrRule:
				""",
				"""
				"""
			);
	}
	
	@Test
	void testOverrideNestedRuleWhileSpecializingType() {
		assertTraversal(
				"FooExtended",
				"""
				type Foo:
					bar Bar (1..1)
						[ruleReference for attr BarAttrRule]
				
				type Bar:
					attr number (1..1)
				
				type FooExtended extends Foo:
					override bar BarExtended (1..1)
						[ruleReference for attr BarAttrRuleOverride]
				
				type BarExtended extends Bar:
					override attr int (1..1)
				
				reporting rule BarAttrRule:
				reporting rule BarAttrRuleOverride:
				""",
				"""
				bar -> attr: BarAttrRuleOverride
				""");
	}
	
	@Test
	void testSiblingTypesDoNotInfluenceEachOther() {
		assertTraversalWithSource(
				"Rules",
				"Bar",
				"""
				type Report:
					foo Foo (1..1)
					bar Bar (1..1)
				
				type Common:
					id string (1..1)
						[ruleReference IdRule]
				
				type Foo extends Common:
					fooAttr string (1..1)
				
				type Bar extends Common:
					barAttr string (1..1)
				
				rule source Rules {
					Foo:
					- id
				}
				
				reporting rule IdRule:
				""",
				"""
				id: IdRule
				""");
	}
	
	@Test
	void testDoNotTraverseInsideAttributeWithEmptyRuleReference() {
		assertTraversal(
				"FooExtended",
				"""
				type Foo:
					bar Bar (1..1)
						[ruleReference BarRule]
					otherBar Bar (1..1)
				
				type Bar:
					attr string (1..1)
						[ruleReference BarAttr]
				
				type FooExtended extends Foo:
					override bar Bar (1..1)
						[ruleReference empty]
				
				reporting rule BarRule:
				reporting rule BarAttr:
				""",
				"""
				bar: <empty>
				otherBar -> attr: BarAttr
				""");
	}
	
	@Test
	void testDoNotTraverseInsideAttributeWithMinusedRuleReference() {
		assertTraversalWithSource(
				"Rules",
				"Foo",
				"""
				type Foo:
					bar Bar (1..1)
						[ruleReference BarRule]
					otherBar Bar (1..1)
				
				type Bar:
					attr string (1..1)
						[ruleReference BarAttr]
				
				rule source Rules {
					Foo:
					- bar
				}
				
				reporting rule BarRule:
				reporting rule BarAttr:
				""",
				"""
				bar: <empty>
				otherBar -> attr: BarAttr
				"""
			);
	}
	
	@Test
	void testValidCircularReferenceTraversal() {
		assertTraversal(
				"Foo",
				"""
    			type Foo:
					bar Bar (1..1)
						[ruleReference for attr AttrRule]
						[ruleReference for bar -> attr DeepAttrRule]
				
				type Bar:
					attr string (1..1)
					bar Bar (0..1)
				
				reporting rule AttrRule:
				reporting rule DeepAttrRule:
    			""",
    			"""
    			bar -> attr: AttrRule
    			bar -> bar -> attr: DeepAttrRule
    			"""
			);
	}
	
	@Test
	void testInvalidCircularReferenceTraversal() {
		assertTraversal(
				"Foo",
				"""
    			type Foo:
					bar Bar (1..1)
						[ruleReference for bar -> attr DeepAttrRule]
				
				type Bar:
					attr string (1..1)
						[ruleReference AttrRule]
					bar Bar (0..1)
				
				reporting rule AttrRule:
				reporting rule DeepAttrRule:
    			""",
    			"""
    			bar -> attr: AttrRule
    			bar -> bar -> attr: DeepAttrRule
    			"""
			);
	}
	
	private void assertTraversal(String typeName, String model, String expected) {
		var parsedModel = modelService.toTestModel(model, false);
		RDataType type = objectFactory.buildRDataType(parsedModel.getType(typeName));
		assertTraversal(null, type, expected);
	}
	private void assertTraversalWithSource(String sourceName, String typeName, String model, String expected) {
		var parsedModel = modelService.toTestModel(model, false);
		RosettaExternalRuleSource source = parsedModel.getRuleSource(sourceName);
		RDataType type = objectFactory.buildRDataType(parsedModel.getType(typeName));
		assertTraversal(source, type, expected);
	}
	private void assertTraversal(RosettaExternalRuleSource source, RDataType type, String expected) {
		String actual = ruleService.traverse(source, type, new StringBuilder(), (acc, context) -> {
			String path = context.getPath().stream().map(a -> a.getName()).collect(Collectors.joining(" -> "));
			String rule;
			if (context.isExplicitlyEmpty()) {
				rule = "<empty>";
			} else {
				rule = context.getRule().getName();
			}
			return acc
				.append(path)
				.append(": ")
				.append(rule)
				.append("\n");
		}).toString().trim();
		
		Assertions.assertEquals(expected.trim(), actual);
	}
}
