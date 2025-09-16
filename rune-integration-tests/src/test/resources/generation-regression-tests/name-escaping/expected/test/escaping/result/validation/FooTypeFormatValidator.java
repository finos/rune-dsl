package test.escaping.result.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.result.Foo;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class FooTypeFormatValidator implements Validator<Foo> {

	private List<ComparisonResult> getComparisonResults(Foo o) {
		return Lists.<ComparisonResult>newArrayList(
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Foo", ValidationResult.ValidationType.TYPE_FORMAT, "Foo", path, "", res.getError());
				}
				return success("Foo", ValidationResult.ValidationType.TYPE_FORMAT, "Foo", path, "");
			})
			.collect(toList());
	}

}
