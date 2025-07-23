package com.regnosys.rosetta.generator.java.condition;

import java.util.stream.Stream;

import javax.inject.Inject;

import com.rosetta.model.lib.validation.Validator;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TypeAliasConditionTest extends AbstractConditionTest {
	@Inject
	private RosettaTestModelService testModelService;
	
	@Test
	void testSimpleTypeAliasCondition() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				typeAlias Foo:
					string
					
					condition C:
						item <> "forbidden"
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		var resultFoo = condition.invoke(RosettaPath.valueOf("p1"), "forbidden");
		var resultBar = condition.invoke(RosettaPath.valueOf("p2"), "value");
		
		assertResults(
			Stream.concat(resultFoo.stream(), resultBar.stream()).toList(),
			(v1) -> assertFailure(v1, "FooC", "p1", "[String] [forbidden] should not equal [String] [forbidden]"),
			(v2) -> assertSuccess(v2, "FooC", "p2")
		);
	}
	
	@Test
	void testSimpleTypeAliasConditionWithFunctionDependency() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				typeAlias Foo:
					string
					
					condition C:
						MyFunc
				
				func MyFunc:
					inputs:
						foo Foo (1..1)
					output:
						result boolean (1..1)
					set result:
						foo <> "forbidden"
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		var resultFoo = condition.invoke(RosettaPath.valueOf("p1"), "forbidden");
		var resultBar = condition.invoke(RosettaPath.valueOf("p2"), "value");
		
		assertResults(
			Stream.concat(resultFoo.stream(), resultBar.stream()).toList(),
			(v1) -> assertFailure(v1, "FooC", "p1", "Condition has failed."),
			(v2) -> assertSuccess(v2, "FooC", "p2")
		);
	}
	
	@Test
	void testTypeAliasConditionUsingTypeParameter() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				typeAlias Foo(p string):
					string
					
					condition C:
						item <> p
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		var resultFoo = condition.invoke(RosettaPath.valueOf("p1"), "value", "value");
		var resultBar = condition.invoke(RosettaPath.valueOf("p2"), "value", "forbidden");
		
		assertResults(
				Stream.concat(resultFoo.stream(), resultBar.stream()).toList(),
				(v1) -> assertFailure(v1, "FooC", "p1", "[String] [value] should not equal [String] [value]"),
				(v2) -> assertSuccess(v2, "FooC", "p2")
			);
	}
	
	@Test
	void testConditionsFromTypeAliasInSingleCardinalityAttributeAreCalled() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					foo Foo (1..1)
				
				typeAlias Foo:
					Bar
					
					condition C1:
						item <> 42
				
				typeAlias Bar:
					int
					
					condition C2:
						item > 0
				""").compile();
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  foo: 0
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "T", "T"),
			(v2) -> assertSuccess(v2, "FooC1", "T.foo"),
			(v3) -> assertFailure(v3, "BarC2", "T.foo", "all elements of paths [Integer] values [0] are not > than all elements of paths [Integer] values [0]")
		);
	}
	
	@Test
	void testConditionsFromTypeAliasInMultiCardinalityAttributeAreCalled() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					foos Foo (0..*)
				
				typeAlias Foo:
					Bar
					
					condition C1:
						item <> 42
				
				typeAlias Bar:
					int
					
					condition C2:
						item > 0
				""").compile();
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  foos: [0, 10, 42]
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "T", "T"),
			(v2) -> assertSuccess(v2, "FooC1", "T.foos(0)"),
			(v3) -> assertFailure(v3, "BarC2", "T.foos(0)", "all elements of paths [Integer] values [0] are not > than all elements of paths [Integer] values [0]"),
			(v4) -> assertSuccess(v4, "FooC1", "T.foos(1)"),
			(v5) -> assertSuccess(v5, "BarC2", "T.foos(1)"),
			(v6) -> assertFailure(v6, "FooC1", "T.foos(2)", "[Integer] [42] should not equal [Integer] [42]"),
			(v7) -> assertSuccess(v7, "BarC2", "T.foos(2)")
		);
	}
	
	@Test
	void testConditionsOnTypeAliasWithUnknownJavaType() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					foos Foo(f: 0) (0..*)
						[metadata scheme]
				
				typeAlias Foo(f int):
					number(fractionalDigits: f)
					
					condition C:
						item <> f
				""").compile();
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  foos: [0, 10, 42]
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "T", "T"),
			(v2) -> assertFailure(v2, "FooC", "T.foos(0)", "[BigDecimal] [0] should not equal [BigDecimal] [0]"),
			(v3) -> assertSuccess(v3, "FooC", "T.foos(1)"),
			(v4) -> assertSuccess(v4, "FooC", "T.foos(2)")
		);
	}
	
	@Test
	void testConditionsOnTypeAliasWithMissingTypeParameter() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					foos Foo (0..*)
						[metadata scheme]
				
				typeAlias Foo(f int):
					number(fractionalDigits: f)
					
					condition C:
						item <> f
				""").compile();
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  foos: [0, 10, 42]
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "FooC", "T.foos(0)"),
			(v2) -> assertSuccess(v2, "FooC", "T.foos(1)"),
			(v3) -> assertSuccess(v3, "FooC", "T.foos(2)")
		);
	}
	
	@Test
	void testConditionsFromTypeWithMultipleAttributes() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					atr1 FooAlias(f: "foo") (0..1)
					atr2 string (0..1)
					
					condition Cond1:
						atr1 exists
				
				typeAlias FooAlias (f string):
					string

					condition CondA:
						f exists
					condition CondB:
						 item <> f
				""").compile();
		
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  atr1: "foo",
				  atr2: "foo2"
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "FooAliasCondA", "T.atr1"),
			(v2) -> assertFailure(v2, "FooAliasCondB", "T.atr1", "[String] [foo] should not equal [String] [foo]")
		);
	}
	
	@Test
	void testConditionsFromTypeWithSingleAttribute() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					atr1 FooAlias(f: "foo") (0..1)
					
					condition Cond1:
						atr1 exists
				
				typeAlias FooAlias (f string):
					string

					condition CondA:
						f exists
					condition CondB:
						 item <> f
				""").compile();
		
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  atr1: "foo"
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "FooAliasCondA", "T.atr1"),
			(v2) -> assertFailure(v2, "FooAliasCondB", "T.atr1", "[String] [foo] should not equal [String] [foo]")
		);
	}
	
	@Test
	void testConditionsFromTypeAliasInMultiCardinalityEmptyAttribute() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type T:
					foos Foo (0..*)
				
				typeAlias Foo:
					Bar
					
					condition C1:
						item <> 42
				
				typeAlias Bar:
					int
					
					condition C2:
						item > 0
				""").compile();
		RosettaModelObject t = model.evaluateExpression(RosettaModelObject.class, """
				T {
				  foos: empty
				}
				""");
		
		var validator = getTypeFormatValidator(t);
		
		var results = ((Validator<?>)validator).getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "T", "T")
		);
	}
}
