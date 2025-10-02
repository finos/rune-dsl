package test.escaping.index.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.index.ResultEscaping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class ResultEscapingTypeFormatValidator implements Validator<ResultEscaping> {

	private List<ComparisonResult> getComparisonResults(ResultEscaping o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, ResultEscaping o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("ResultEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "ResultEscaping", path, "", res.getError());
				}
				return success("ResultEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "ResultEscaping", path, "");
			})
			.collect(toList());
	}

}
