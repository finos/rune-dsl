package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.List;
import test.pojo.Qux;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class QuxValidator implements Validator<Qux> {

	private List<ComparisonResult> getComparisonResults(Qux o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("qux", (FieldWithMetaString) o.getQux() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Qux o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Qux", ValidationResult.ValidationType.CARDINALITY, "Qux", path, "", res.getError());
				}
				return success("Qux", ValidationResult.ValidationType.CARDINALITY, "Qux", path, "");
			})
			.collect(toList());
	}

}
