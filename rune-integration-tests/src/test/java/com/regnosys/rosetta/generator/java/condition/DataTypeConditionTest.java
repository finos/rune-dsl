package com.regnosys.rosetta.generator.java.condition;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class DataTypeConditionTest extends AbstractConditionTest {
	@Inject
	private RosettaTestModelService testModelService;

	@Test
	void emptyConditionIsSuccess() {	
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					a int (0..1)
					
					condition C:
					    if a exists
					    then empty
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");

		RosettaModelObject foo = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    a: 10
				}
				""");
		
		var fooResults = condition.invoke(RosettaPath.valueOf("foo"), foo);
		
		assertResults(
			fooResults,
			(v1) -> assertSuccess(v1, "FooC", "foo")
		);
	}
	
	@Test
	void testChoiceCondition() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Test:
					field1 string (0..1)
					field2 string (0..2)
				
					condition RequiredChoice:
						required choice field1, field2
				
					condition OptionalChoice:
						optional choice field1, field2
				""").compile();
		
		var reqCondition = getCondition(model, "Test", "RequiredChoice");
		var optCondition = getCondition(model, "Test", "OptionalChoice");
		
		RosettaModelObject fullyPopulated = model.evaluateExpression(RosettaModelObject.class, """
				Test {
					field1: "field 1 value",
					field2: "field 2 value"
				}
				""");
		RosettaModelObject partiallyPopulated = model.evaluateExpression(RosettaModelObject.class, """
				Test {
					field1: "field 1 value",
					...
				}
				""");
		
		var fullyRequired = reqCondition.invoke(RosettaPath.valueOf("full"), fullyPopulated);
		var partiallyRequired = reqCondition.invoke(RosettaPath.valueOf("partial"), partiallyPopulated);
		var fullyOptional = optCondition.invoke(RosettaPath.valueOf("full"), fullyPopulated);
		var partiallyOptional = optCondition.invoke(RosettaPath.valueOf("partial"), partiallyPopulated);
		
		assertResults(
			Streams.concat(fullyRequired.stream(), partiallyRequired.stream(), fullyOptional.stream(), partiallyOptional.stream()).toList(),
			(v1) -> assertFailure(v1, "TestRequiredChoice", "full", "One and only one field must be set of 'field1', 'field2'. Set fields are 'field1', 'field2'."),
			(v2) -> assertSuccess(v2, "TestRequiredChoice", "partial"),
			(v3) -> assertFailure(v3, "TestOptionalChoice", "full", "Zero or one field must be set of 'field1', 'field2'. Set fields are 'field1', 'field2'."),
			(v4) -> assertSuccess(v4, "TestOptionalChoice", "partial")
		);
	}
	
	@Test
	void inapplicableConditionIsSuccess() {	
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					a int (0..1)
					
					condition C:
					    if a exists
					    then a = 42
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");

		RosettaModelObject foo = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    ...
				}
				""");
		
		var fooResults = condition.invoke(RosettaPath.valueOf("foo"), foo);
		
		assertResults(
			fooResults,
			(v1) -> assertSuccess(v1, "FooC", "foo")
		);
	}
	
	@Test
	void dateComparisonConditionOnTypeWithMetaTest() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					[metadata key]
					a date (1..1)
					b date (1..1)
					
					condition DateCondition:
						a <= b
				""").compile();
		
		var condition = getCondition(model, "Foo", "DateCondition");
		
		RosettaModelObject foo = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    a: date {
							year: 2024,
							month: 1,
							day: 1
						},
					b: date {
							year: 2024,
							month: 6,
							day: 30
						}
				}
				""");
		
		assertResults(
			condition.invoke(RosettaPath.valueOf("foo"), foo),
			(v1) -> assertSuccess(v1, "FooDateCondition", "foo")
		);
	}
	
	@Test
	void omittedParameterInConditionTest() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					a int (0..1)
					
					condition C:
					    FooIsValid
				
				func FooIsValid:
					inputs: foo Foo (1..1)
					output: result boolean (1..1)
					set result:
						foo -> a exists
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		RosettaModelObject foo1 = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    a: 42
				}
				""");
		RosettaModelObject foo2 = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    ...
				}
				""");
		
		var foo1Results = condition.invoke(RosettaPath.valueOf("foo1"), foo1);
		var foo2Results = condition.invoke(RosettaPath.valueOf("foo2"), foo2);
		
		assertResults(
			Streams.concat(foo1Results.stream(), foo2Results.stream()).toList(),
			(v1) -> assertSuccess(v1, "FooC", "foo1"),
			(v2) -> assertFailure(v2, "FooC", "foo2", "Condition has failed.")
		);
	}
	
	@Test
	void useImplicitVariableInConditionTest() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					a int (0..1)
					
					condition C:
					    item -> a exists and [item, item] any = item
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		RosettaModelObject foo1 = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    a: 42
				}
				""");
		RosettaModelObject foo2 = model.evaluateExpression(RosettaModelObject.class, """
				Foo {
				    ...
				}
				""");
		
		var foo1Results = condition.invoke(RosettaPath.valueOf("foo1"), foo1);
		var foo2Results = condition.invoke(RosettaPath.valueOf("foo2"), foo2);
		
		assertResults(
			Streams.concat(foo1Results.stream(), foo2Results.stream()).toList(),
			(v1) -> assertSuccess(v1, "FooC", "foo1"),
			(v2) -> assertFailure(v2, "FooC", "foo2", "[Foo->getA] does not exist")
		);
	}
	
	@Test
	void shouldCheckConditionWithInheritedAttribute() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type Foo:
					x string (0..1)
					y string (0..1)
					
					condition A:
						x exists
				
				type Bar extends Foo:
					z string (0..1)
					
					condition B:
						y exists
				""").compile();
		
		var condition = getCondition(model, "Bar", "B");
		
		RosettaModelObject bar1 = model.evaluateExpression(RosettaModelObject.class, """
				Bar {
				    z: "v1",
				    ...
				}
				""");
		RosettaModelObject bar2 = model.evaluateExpression(RosettaModelObject.class, """
				Bar {
				    y: "v1",
				    z: "v2",
				    ...
				}
				""");
		
		var bar1Results = condition.invoke(RosettaPath.valueOf("bar1"), bar1);
		var bar2Results = condition.invoke(RosettaPath.valueOf("bar2"), bar2);
		
		assertResults(
			Streams.concat(bar1Results.stream(), bar2Results.stream()).toList(),
			(v1) -> assertFailure(v1, "BarB", "bar1", "[Bar->getY] does not exist"),
			(v2) -> assertSuccess(v2, "BarB", "bar2")
		);
	}
}
