package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Level1;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperators.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class Level1TypeFormatValidator implements Validator<Level1> {

	private List<ComparisonResult> getComparisonResults(Level1 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("attr", o.getAttr(), empty(), of(0), empty(), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Level1 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Level1", ValidationResult.ValidationType.TYPE_FORMAT, "Level1", path, "", res.getError());
				}
				return success("Level1", ValidationResult.ValidationType.TYPE_FORMAT, "Level1", path, "");
			})
			.collect(toList());
	}

}
