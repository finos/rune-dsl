package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.Leaf;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class LeafTypeFormatValidator implements Validator<Leaf> {

	private List<ComparisonResult> getComparisonResults(Leaf o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Leaf o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Leaf", ValidationResult.ValidationType.TYPE_FORMAT, "Leaf", path, "", res.getError());
				}
				return success("Leaf", ValidationResult.ValidationType.TYPE_FORMAT, "Leaf", path, "");
			})
			.collect(toList());
	}

}
