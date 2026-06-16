package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.Option1;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class Option1TypeFormatValidator implements Validator<Option1> {

	private List<ComparisonResult> getComparisonResults(Option1 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("only1", o.getOnly1(), empty(), of(0), empty(), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Option1 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Option1", ValidationResult.ValidationType.TYPE_FORMAT, "Option1", path, "", res.getError());
				}
				return success("Option1", ValidationResult.ValidationType.TYPE_FORMAT, "Option1", path, "");
			})
			.collect(toList());
	}

}
