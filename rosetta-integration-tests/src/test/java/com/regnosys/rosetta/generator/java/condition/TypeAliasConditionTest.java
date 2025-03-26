package com.regnosys.rosetta.generator.java.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ReflectiveInvoker;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TypeAliasConditionTest {
	@Inject
	private RosettaTestModelService testModelService;
	@Inject
	private ValidatorFactory validatorFactory;
	
	@Test
	void testSimpleTypeAliasCondition() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				typeAlias Foo:
					string
					
					condition C:
						item <> "Foo"
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		ValidationResult<?> resultFoo = condition.invoke(RosettaPath.valueOf("foo"), "Foo").get(0);
		ValidationResult<?> resultBar = condition.invoke(RosettaPath.valueOf("foo"), "Bar").get(0);
		
		Assertions.assertAll(
			() -> assertFalse(resultFoo.isSuccess(), "Foo"),
			() -> assertTrue(resultBar.isSuccess(), "Bar")
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
						foo <> "Foo"
				""").compile();
		
		var condition = getCondition(model, "Foo", "C");
		
		ValidationResult<?> resultFoo = condition.invoke(RosettaPath.valueOf("path"), "Foo").get(0);
		ValidationResult<?> resultBar = condition.invoke(RosettaPath.valueOf("path"), "Bar").get(0);
		
		Assertions.assertAll(
			() -> assertFalse(resultFoo.isSuccess(), "Foo"),
			() -> assertTrue(resultBar.isSuccess(), "Bar")
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
		
		ValidationResult<?> resultFoo = condition.invoke(RosettaPath.valueOf("path"), "Foo", "Foo").get(0);
		ValidationResult<?> resultBar = condition.invoke(RosettaPath.valueOf("path"), "Bar", "Foo").get(0);
				
		Assertions.assertAll(
			() -> assertFalse(resultFoo.isSuccess(), "Foo"),
			() -> assertTrue(resultBar.isSuccess(), "Bar")
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
		Validator<RosettaModelObject> validator = getTypeFormatValidator(t);
		
		List<ValidationResult<?>> results = validator.getValidationResults(RosettaPath.valueOf("T"), t);
		
		assertResults(
			results,
			(first) -> assertSuccess(first, "FooC1", "T.foo"),
			(second) -> assertFailure(second, "BarC2", "T.foo", "all elements of paths [Integer] values [0] are not > than all elements of paths [Integer] values [0]")
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
		Validator<RosettaModelObject> validator = getTypeFormatValidator(t);
		
		List<ValidationResult<?>> results = validator.getValidationResults(RosettaPath.valueOf("T"), t);
		assertResults(
			results,
			(v1) -> assertSuccess(v1, "FooC1", "T.foos(0)"),
			(v2) -> assertFailure(v2, "BarC2", "T.foos(0)", "all elements of paths [Integer] values [0] are not > than all elements of paths [Integer] values [0]"),
			(v3) -> assertSuccess(v3, "FooC1", "T.foos(1)"),
			(v4) -> assertSuccess(v4, "BarC2", "T.foos(1)"),
			(v5) -> assertFailure(v5, "FooC1", "T.foos(2)", "[Integer] [42] should not equal [Integer] [42]"),
			(v6) -> assertSuccess(v6, "BarC2", "T.foos(2)")
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
		Validator<RosettaModelObject> validator = getTypeFormatValidator(t);
		
		List<ValidationResult<?>> results = validator.getValidationResults(RosettaPath.valueOf("T"), t);
		assertResults(
			results,
			(v1) -> assertFailure(v1, "FooC", "T.foos(0)", "[BigDecimal] [0] should not equal [BigDecimal] [0]"),
			(v2) -> assertSuccess(v2, "FooC", "T.foos(1)"),
			(v3) -> assertSuccess(v3, "FooC", "T.foos(2)")
		);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ReflectiveInvoker<List<ValidationResult<?>>> getCondition(JavaTestModel model, String typeName, String conditionName) {
		Object condition = model.getConditionJavaInstance(typeName, conditionName);
		return (ReflectiveInvoker<List<ValidationResult<?>>>)(ReflectiveInvoker)ReflectiveInvoker.from(condition, "getValidationResults", List.class);
	}
	
	@SuppressWarnings("unchecked")
	private Validator<RosettaModelObject> getTypeFormatValidator(RosettaModelObject object) {
		return (Validator<RosettaModelObject>) object.metaData().typeFormatValidator(validatorFactory);
	}
	
	private void assertSuccess(ValidationResult<?> validationResult, String validationName, String path) {
		Assertions.assertAll(
			"For " + validationName + " on " + path,
			() -> assertTrue(validationResult.isSuccess(), "Expected a succes, but was a failure."),
			() -> assertEquals(validationName, validationResult.getName(), "Validation names did not match."),
			() -> assertEquals(RosettaPath.valueOf(path), validationResult.getPath(), "Paths did not match.")
		);
	}
	private void assertFailure(ValidationResult<?> validationResult, String validationName, String path, String failureReason) {
		Assertions.assertAll(
			"For " + validationName + " on " + path,
			() -> assertFalse(validationResult.isSuccess(), "Expected a failure, but was a success"),
			() -> assertEquals(validationName, validationResult.getName(), "Validation names did not match."),
			() -> assertEquals(RosettaPath.valueOf(path), validationResult.getPath(), "Paths did not match."),
			() -> {
				if (!validationResult.isSuccess()) {
					assertEquals(failureReason, validationResult.getFailureReason().orElse(null), "Failure reasons did not match.");
				}
			}
		);
	}
	@SafeVarargs
	private void assertResults(List<ValidationResult<?>> results, Consumer<ValidationResult<?>>... assertions) {
		List<Executable> assertionsWithListBoundChecks = new ArrayList<>();
		assertionsWithListBoundChecks.add(() -> assertEquals(assertions.length, results.size(), "Expected " + assertions.length + " validation result(s), but were " + results.size()));
		for (int i=0; i<assertions.length; i++) {
			int index = i;
			assertionsWithListBoundChecks.add(() -> {
				if (index < results.size()) {
					assertions[index].accept(results.get(index));
				}
			});
		}
		Assertions.assertAll(assertionsWithListBoundChecks);
	}
}
