package test.escaping.getclass.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.getclass.GetClassEscaping;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperators.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class GetClassEscapingTypeFormatValidator implements Validator<GetClassEscaping> {

	private List<ComparisonResult> getComparisonResults(GetClassEscaping o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("class", o._getClass(), empty(), of(0), empty(), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, GetClassEscaping o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("GetClassEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "GetClassEscaping", path, "", res.getError());
				}
				return success("GetClassEscaping", ValidationResult.ValidationType.TYPE_FORMAT, "GetClassEscaping", path, "");
			})
			.collect(toList());
	}

}
