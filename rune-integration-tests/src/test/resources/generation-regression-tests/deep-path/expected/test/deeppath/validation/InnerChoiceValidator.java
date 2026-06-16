package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.InnerChoice;
import test.deeppath.Option1;
import test.deeppath.Option2;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class InnerChoiceValidator implements Validator<InnerChoice> {

	private List<ComparisonResult> getComparisonResults(InnerChoice o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("Option1", (Option1) o.getOption1() != null ? 1 : 0, 0, 1), 
				checkCardinality("Option2", (Option2) o.getOption2() != null ? 1 : 0, 0, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, InnerChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("InnerChoice", ValidationResult.ValidationType.CARDINALITY, "InnerChoice", path, "", res.getError());
				}
				return success("InnerChoice", ValidationResult.ValidationType.CARDINALITY, "InnerChoice", path, "");
			})
			.collect(toList());
	}

}
