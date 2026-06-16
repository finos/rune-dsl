package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.Option2;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class Option2Validator implements Validator<Option2> {

	private List<ComparisonResult> getComparisonResults(Option2 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("common", (String) o.getCommon() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Option2 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Option2", ValidationResult.ValidationType.CARDINALITY, "Option2", path, "", res.getError());
				}
				return success("Option2", ValidationResult.ValidationType.CARDINALITY, "Option2", path, "");
			})
			.collect(toList());
	}

}
