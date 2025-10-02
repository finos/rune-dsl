package test.escaping.gettype.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.gettype.GetTypeEscaping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperators.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class GetTypeEscapingValidator implements Validator<GetTypeEscaping> {

	private List<ComparisonResult> getComparisonResults(GetTypeEscaping o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("type", (Integer) o._getType() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, GetTypeEscaping o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("GetTypeEscaping", ValidationResult.ValidationType.CARDINALITY, "GetTypeEscaping", path, "", res.getError());
				}
				return success("GetTypeEscaping", ValidationResult.ValidationType.CARDINALITY, "GetTypeEscaping", path, "");
			})
			.collect(toList());
	}

}
