package test.deeppath.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.deeppath.InnerChoice;
import test.deeppath.Leaf;
import test.deeppath.OuterChoice;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class OuterChoiceValidator implements Validator<OuterChoice> {

	private List<ComparisonResult> getComparisonResults(OuterChoice o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("InnerChoice", (InnerChoice) o.getInnerChoice() != null ? 1 : 0, 0, 1), 
				checkCardinality("Leaf", (Leaf) o.getLeaf() != null ? 1 : 0, 0, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, OuterChoice o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("OuterChoice", ValidationResult.ValidationType.CARDINALITY, "OuterChoice", path, "", res.getError());
				}
				return success("OuterChoice", ValidationResult.ValidationType.CARDINALITY, "OuterChoice", path, "");
			})
			.collect(toList());
	}

}
