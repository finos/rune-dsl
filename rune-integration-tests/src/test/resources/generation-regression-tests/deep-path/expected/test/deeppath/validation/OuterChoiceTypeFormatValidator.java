package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.OuterChoice;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class OuterChoiceTypeFormatValidator implements Validator<OuterChoice> {

	private List<ComparisonResult> getComparisonResults(OuterChoice o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, OuterChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("OuterChoice", ValidationResult.ValidationType.TYPE_FORMAT, "OuterChoice", path, "", res.getError());
				}
				return success("OuterChoice", ValidationResult.ValidationType.TYPE_FORMAT, "OuterChoice", path, "");
			})
			.collect(toList());
	}

}
