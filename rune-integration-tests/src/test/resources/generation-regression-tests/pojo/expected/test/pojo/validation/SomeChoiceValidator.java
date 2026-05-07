package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Bar;
import test.pojo.Foo;
import test.pojo.SomeChoice;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class SomeChoiceValidator implements Validator<SomeChoice> {

	private List<ComparisonResult> getComparisonResults(SomeChoice o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("Foo", (Foo) o.getFoo() != null ? 1 : 0, 0, 1), 
				checkCardinality("Bar", (Bar) o.getBar() != null ? 1 : 0, 0, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, SomeChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("SomeChoice", ValidationResult.ValidationType.CARDINALITY, "SomeChoice", path, "", res.getError());
				}
				return success("SomeChoice", ValidationResult.ValidationType.CARDINALITY, "SomeChoice", path, "");
			})
			.collect(toList());
	}

}
