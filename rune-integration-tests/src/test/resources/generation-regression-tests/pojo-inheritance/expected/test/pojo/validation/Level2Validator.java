package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Level2;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class Level2Validator implements Validator<Level2> {

	private List<ComparisonResult> getComparisonResults(Level2 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Level2 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Level2", ValidationResult.ValidationType.CARDINALITY, "Level2", path, "", res.getError());
				}
				return success("Level2", ValidationResult.ValidationType.CARDINALITY, "Level2", path, "");
			})
			.collect(toList());
	}

}
