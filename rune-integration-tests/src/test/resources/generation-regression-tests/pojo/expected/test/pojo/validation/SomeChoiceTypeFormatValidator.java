package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.SomeChoice;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class SomeChoiceTypeFormatValidator implements Validator<SomeChoice> {

	private List<ComparisonResult> getComparisonResults(SomeChoice o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, SomeChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("SomeChoice", ValidationResult.ValidationType.TYPE_FORMAT, "SomeChoice", path, "", res.getError());
				}
				return success("SomeChoice", ValidationResult.ValidationType.TYPE_FORMAT, "SomeChoice", path, "");
			})
			.collect(toList());
	}

}
