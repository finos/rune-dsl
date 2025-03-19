package com.rosetta.test.model.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.test.model.Pojo;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperators.checkString;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class PojoTypeFormatValidator implements Validator<Pojo> {

	private List<ComparisonResult> getComparisonResults(Pojo o) {
		return Lists.<ComparisonResult>newArrayList(
				checkString("simpleAttr", o.getSimpleAttr(), 0, of(42), empty()), 
				checkString("multiSimpleAttr", o.getMultiSimpleAttr(), 0, of(42), empty())
			);
	}

	@Override
	public ValidationResult<Pojo> validate(RosettaPath path, Pojo o) {
		String error = getComparisonResults(o)
			.stream()
			.filter(res -> !res.get())
			.map(res -> res.getError())
			.collect(joining("; "));

		if (!isNullOrEmpty(error)) {
			return failure("Pojo", ValidationType.TYPE_FORMAT, "Pojo", path, "", error);
		}
		return success("Pojo", ValidationType.TYPE_FORMAT, "Pojo", path, "");
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Pojo o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Pojo", ValidationType.TYPE_FORMAT, "Pojo", path, "", res.getError());
				}
				return success("Pojo", ValidationType.TYPE_FORMAT, "Pojo", path, "");
			})
			.collect(toList());
	}

}
