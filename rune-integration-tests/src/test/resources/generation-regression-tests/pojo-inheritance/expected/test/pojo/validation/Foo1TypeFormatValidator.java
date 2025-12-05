package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Foo1;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class Foo1TypeFormatValidator implements Validator<Foo1> {

	private List<ComparisonResult> getComparisonResults(Foo1 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("attr", o.getAttr(), empty(), of(0), empty(), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo1 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Foo1", ValidationResult.ValidationType.TYPE_FORMAT, "Foo1", path, "", res.getError());
				}
				return success("Foo1", ValidationResult.ValidationType.TYPE_FORMAT, "Foo1", path, "");
			})
			.collect(toList());
	}

}
