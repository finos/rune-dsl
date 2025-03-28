package com.regnosys.rosetta.generator.java.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.util.ReflectiveInvoker;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;

public class AbstractConditionTest {
	@Inject
	private ValidatorFactory validatorFactory;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ReflectiveInvoker<List<ValidationResult<?>>> getCondition(JavaTestModel model, String typeName, String conditionName) {
		Object condition = model.getConditionJavaInstance(typeName, conditionName);
		return (ReflectiveInvoker<List<ValidationResult<?>>>)(ReflectiveInvoker)ReflectiveInvoker.from(condition, "getValidationResults", List.class);
	}
	
	@SuppressWarnings("unchecked")
	protected Validator<RosettaModelObject> getTypeFormatValidator(RosettaModelObject object) {
		return (Validator<RosettaModelObject>) object.metaData().typeFormatValidator(validatorFactory);
	}
	
	protected void assertSuccess(ValidationResult<?> validationResult, String validationName, String path) {
		Assertions.assertAll(
			"For " + validationName + " on " + path,
			() -> assertTrue(validationResult.isSuccess(), "Expected a succes, but was a failure."),
			() -> assertEquals(validationName, validationResult.getName(), "Validation names did not match."),
			() -> assertEquals(RosettaPath.valueOf(path), validationResult.getPath(), "Paths did not match.")
		);
	}
	protected void assertFailure(ValidationResult<?> validationResult, String validationName, String path, String failureReason) {
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
	protected final void assertResults(List<ValidationResult<?>> results, Consumer<ValidationResult<?>>... assertions) {
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
