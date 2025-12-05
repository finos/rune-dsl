package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.ReferenceWithMetaString;
import java.util.List;
import test.pojo.Baz;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class BazValidator implements Validator<Baz> {

	private List<ComparisonResult> getComparisonResults(Baz o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("baz", (ReferenceWithMetaString) o.getBaz() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Baz o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Baz", ValidationResult.ValidationType.CARDINALITY, "Baz", path, "", res.getError());
				}
				return success("Baz", ValidationResult.ValidationType.CARDINALITY, "Baz", path, "");
			})
			.collect(toList());
	}

}
