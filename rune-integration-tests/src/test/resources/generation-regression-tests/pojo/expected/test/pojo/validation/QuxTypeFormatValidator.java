package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Qux;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class QuxTypeFormatValidator implements Validator<Qux> {

	private List<ComparisonResult> getComparisonResults(Qux o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Qux o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Qux", ValidationResult.ValidationType.TYPE_FORMAT, "Qux", path, "", res.getError());
				}
				return success("Qux", ValidationResult.ValidationType.TYPE_FORMAT, "Qux", path, "");
			})
			.collect(toList());
	}

}
