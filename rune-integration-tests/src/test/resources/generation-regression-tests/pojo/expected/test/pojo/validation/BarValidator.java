package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Bar;
import test.pojo.Qux;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class BarValidator implements Validator<Bar> {

	private List<ComparisonResult> getComparisonResults(Bar o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("bar", (Qux) o.getBar() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Bar o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Bar", ValidationResult.ValidationType.CARDINALITY, "Bar", path, "", res.getError());
				}
				return success("Bar", ValidationResult.ValidationType.CARDINALITY, "Bar", path, "");
			})
			.collect(toList());
	}

}
