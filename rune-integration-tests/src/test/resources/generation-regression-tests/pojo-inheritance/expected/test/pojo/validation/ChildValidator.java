package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Child;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class ChildValidator implements Validator<Child> {

	private List<ComparisonResult> getComparisonResults(Child o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Child o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Child", ValidationResult.ValidationType.CARDINALITY, "Child", path, "", res.getError());
				}
				return success("Child", ValidationResult.ValidationType.CARDINALITY, "Child", path, "");
			})
			.collect(toList());
	}

}
