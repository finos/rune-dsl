package test.escaping.gettype.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.gettype.GetTypeEscaping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class GetTypeEscapingTypeFormatValidator implements Validator<GetTypeEscaping> {

	private List<ComparisonResult> getComparisonResults(GetTypeEscaping o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("type", o._getType(), empty(), of(0), empty(), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, GetTypeEscaping o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("GetTypeEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "GetTypeEscaping", path, "", res.getError());
				}
				return success("GetTypeEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "GetTypeEscaping", path, "");
			})
			.collect(toList());
	}

}
