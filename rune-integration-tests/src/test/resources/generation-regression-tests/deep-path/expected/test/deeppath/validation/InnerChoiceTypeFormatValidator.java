package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.InnerChoice;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class InnerChoiceTypeFormatValidator implements Validator<InnerChoice> {

	private List<ComparisonResult> getComparisonResults(InnerChoice o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, InnerChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("InnerChoice", ValidationResult.ValidationType.TYPE_FORMAT, "InnerChoice", path, "", res.getError());
				}
				return success("InnerChoice", ValidationResult.ValidationType.TYPE_FORMAT, "InnerChoice", path, "");
			})
			.collect(toList());
	}

}
